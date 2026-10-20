
using BCrypt.Net;
using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.Win32;
using MimeKit;
using Npgsql;
using Org.BouncyCastle.Asn1.Ocsp;
using sport_app_server.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Net.Mail;
using System.Security.Claims;
using System.Security.Cryptography.X509Certificates;
using System.Text;

namespace sport_app_server
{
    public record LoginRecord(string Login, string Email, string Password, string? Phone);
    public record LoginAnswner(string JWT);
    // public record PasswordResetRequest(string Email, string CodeToken, string Code, string ResetTocken);
    public class Program
    {
        
        public static void Main(string[] args)
        {
            string secretKey = "EvNFHhhBqz3kL5zwE60v7V6Knnwf6lP4"; // Ключ для JWT, да, я знаю что его так хранить в репозитории небезопасно, но пока не релиз версия --- катит
            string issuer = "sport-app-server";
            string audience = "sport-app-client";

            var builder = WebApplication.CreateBuilder(args);

            builder.Services.AddDbContext<sportappContext>(options =>
                options.UseNpgsql("Host=localhost;Database=sport-app;Username=postgres;Password=13495;SearchPath=public")); // Здесь точно так же как и с JWT
            builder.Services
                .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
                .AddJwtBearer(options =>
                {
                    options.TokenValidationParameters = new TokenValidationParameters
                    {
                        ValidateIssuer = true,
                        ValidIssuer = issuer,
                        ValidateAudience = true,
                        ValidAudience = audience,
                        ValidateLifetime = true,
                        ValidateIssuerSigningKey = true,
                        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey))
                    };
                });

            builder.Services.AddAuthorization();

            builder.Services.AddOpenApi();

            var app = builder.Build();

            if (app.Environment.IsDevelopment())
            {
                app.MapOpenApi();
            }

            app.UseHttpsRedirection();

            app.UseAuthentication();
            app.UseAuthorization();

            app.MapPost("/api/auth/register", async ([FromBody] LoginRecord request, sportappContext db) =>
            {
                var errors = new Dictionary<string, string[]>();
                CheckResult loginCheck = Tools.CheckLogin(request.Login);
                CheckResult emailCheck = Tools.CheckEmail(request.Email);
                CheckResult passwordCheck = Tools.CheckPassword(request.Password);
                CheckResult phoneCheck = Tools.CheckPhone(request.Phone);

                if (loginCheck.error)
                {
                    errors["login"] = new[] { loginCheck.errorMessage };
                }
                if(emailCheck.error)
                {
                    errors["email"] = new[] { emailCheck.errorMessage };
                }
                if (passwordCheck.error)
                {
                    errors["password"] = new[] { passwordCheck.errorMessage };
                }
                if (phoneCheck.error)
                {
                    errors["phone"] = new[] { phoneCheck.errorMessage };
                }
                
                var users = await db.Users.ToListAsync();
                int findLogin = (from user in users where user.Nickname == request.Login select user).Count();
                if (findLogin != 0)
                {
                    errors["login"] = new[] { "Регистрация невозможна, текущий логин занят" };
                }
                if(!string.IsNullOrEmpty(request.Phone))
                {
                    int findPhone = (from user in users where user.PhoneNumber == request.Phone select user).Count();
                    if (findPhone != 0)
                    {
                        errors["phone"] = new[] { "Регистрация невозможна, текущий номер занят" };
                    }
                }
                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                User newUser = new User();
                newUser.Id = Guid.NewGuid();
                newUser.Nickname = request.Login;
                newUser.Balance = 0;
                newUser.DaysStraight = 0;
                newUser.Email = request.Email;
                if (!string.IsNullOrEmpty(request.Phone))
                    newUser.PhoneNumber = request.Phone;
                newUser.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password, workFactor: 12);
                newUser.CreatedAt = DateTime.UtcNow;

                db.Users.Add(newUser);
                await db.SaveChangesAsync();

                string jwt = GenerateJwtToken(newUser, secretKey, issuer, audience);
                LoginAnswner loginAnswner = new LoginAnswner(jwt);
                return Results.Ok(loginAnswner);
            });
            app.MapPost("/api/auth/login", async ([FromBody] LoginRecord request, sportappContext db) =>
            {
                var errors = new Dictionary<string, string[]>();
                CheckResult loginCheck = Tools.CheckLogin(request.Login);
                CheckResult emailCheck = Tools.CheckEmail(request.Email);
                CheckResult passwordCheck = Tools.CheckPassword(request.Password);
                CheckResult phoneCheck = Tools.CheckPhone(request.Phone);

                var users = await db.Users.ToListAsync();
                User? currentUser = null;
                if (!loginCheck.error && !passwordCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Nickname == request.Login);
                }
                else if (!emailCheck.error && !passwordCheck.error)
                { 
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Email == request.Email);
                }
                else if (!string.IsNullOrEmpty(request.Phone) && !phoneCheck.error && !passwordCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.PhoneNumber == request.Phone);
                }
                else
                {
                    errors["authorisation"] = new[] { "Не данные введены не корректно" };
                }

                bool errFlag = false;
                if (currentUser == null)
                    errFlag = true;
                else
                {
                    if (!BCrypt.Net.BCrypt.Verify(request.Password, currentUser.PasswordHash))
                        errFlag = true;
                }

                if (errFlag)
                    errors["authorisation"] = new[] { "Пользователя с такими данными не существует" };
                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                await db.SaveChangesAsync();

                string jwt = GenerateJwtToken(currentUser, secretKey, issuer, audience);
                LoginAnswner loginAnswner = new LoginAnswner(jwt);
                return Results.Ok(loginAnswner);
            });
            app.MapGet("/api/users/me", async (ClaimsPrincipal user, sportappContext db) =>
            {
                var userName = user.FindFirst(ClaimTypes.Name)?.Value;
                User currentUser = await db.Users.FirstOrDefaultAsync(user => user.Nickname == userName);
                return Results.Ok(new
                {
                    login = currentUser.Nickname,
                    phone = currentUser.PhoneNumber,
                    email = currentUser.Email,
                    createdAt = currentUser.CreatedAt,
                    balance = currentUser.Balance,
                    daysStraight = currentUser.DaysStraight
                });
            }).RequireAuthorization();
            //----------------------Заготовка сброса пароля-------------------------------------------------------------------------
            //app.MapGet("/api/auth/forgot-password/request", async ([FromBody] PasswordResetRequest request, sportappContext db) =>
            //{
            //    var errors = new Dictionary<string, string[]>();
            //    CheckResult emailCheck = Tools.CheckEmail(request.Email);
            //    if (emailCheck.error)
            //        errors["email"] = new[] { emailCheck.errorMessage };


            //    if (errors.Count > 0)
            //        return Results.ValidationProblem(errors);


            //    return Results.Ok();

            //});

            //-----------------Тестовая часть для отправки email-----------------------------------------------------
            //app.MapGet("/test-email", async (ClaimsPrincipal user, sportappContext db) =>
            //{
            //    try
            //    {
            //        var userName = user.FindFirst(ClaimTypes.Name)?.Value;
            //        User currentUser = await db.Users.FirstOrDefaultAsync(user => user.Nickname == userName);
            //        var message = new MimeMessage();
            //        message.From.Add(new MailboxAddress("Sport App", "notifi.sportakus@gmail.com"));
            //        message.To.Add(new MailboxAddress(currentUser.Nickname, currentUser.Email));
            //        message.Subject = "Тестовое письмо из Sport App";

            //        message.Body = new TextPart("html")
            //        {
            //            Text = "<h1>Привет!</h1><p>Это тестовое письмо, отправленное из C# через Gmail SMTP.</p>"
            //        };

            //        using var client = new MailKit.Net.Smtp.SmtpClient();

            //        await client.ConnectAsync("smtp.gmail.com", 587, SecureSocketOptions.StartTls);

            //        await client.AuthenticateAsync("notifi.sportakus@gmail.com", "secret_key");

            //        await client.SendAsync(message);
            //        await client.DisconnectAsync(true);

            //        return Results.Ok("Письмо успешно отправлено!");
            //    }
            //    catch (Exception ex)
            //    {
            //        return Results.Problem(detail: ex.ToString(), statusCode: 500);
            //    }
            //}).RequireAuthorization();
            
            app.Run();
        }
        
        private static string GenerateJwtToken(User user, string secretKey, string issuer, string audience)
        {
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Nickname),
                new Claim(ClaimTypes.Role, "user")
            };

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey));
            var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
                issuer: issuer,
                audience: audience,
                claims: claims,
                expires: DateTime.UtcNow.AddMinutes(5),
                signingCredentials: credentials
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
