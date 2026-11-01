
using BCrypt.Net;
using DotNetEnv;
using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Memory;
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
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace sport_app_server
{
    public record LoginRecord(string Login, string Email, string Password, string? Phone, string? EmailCode);
    public record LoginAnswner(string JWT);
    public record EmailConfirmRequest(string Login, string Email);
    // public record PasswordResetRequest(string Email, string CodeToken, string Code, string ResetTocken);
    public class Program
    {
        
        public static void Main(string[] args)
        {


            var builder = WebApplication.CreateBuilder(args);

            Env.Load();
            string secretKey    = Environment.GetEnvironmentVariable("JWT_SECRET_KEY") ?? throw new InvalidOperationException("Ключ не найден");
            string issuer       = Environment.GetEnvironmentVariable("JWT_ISSUER") ?? throw new InvalidOperationException("JWT_ISSUER не найден");
            string audience     = Environment.GetEnvironmentVariable("JWT_AUDIENCE") ?? throw new InvalidOperationException("JWT_AUDIENCE не найден");
            int jwtExpirationMinutes;
            string emailKey = Environment.GetEnvironmentVariable("EMAIL_KEY") ?? throw new InvalidOperationException("Данные для подключения к базе не найдены");
            string postgressDB = Environment.GetEnvironmentVariable("POSTGRES_DB") ?? throw new InvalidOperationException("Имя базы не найдено");
            string postgressUser = Environment.GetEnvironmentVariable("POSTGRES_USER") ?? throw new InvalidOperationException("Имя пользователя базы не найдено");
            string postgressPassword = Environment.GetEnvironmentVariable("POSTGRES_PASSWORD") ?? throw new InvalidOperationException("Пароль базы не найдено");
            string postgressHost = Environment.GetEnvironmentVariable("POSTGRES_HOST") ?? throw new InvalidOperationException("Хост базы не найден");
            string exstraParams = Environment.GetEnvironmentVariable("EXTRA_PARAMS") ?? "";

            string databaseConnectParams = $"Host={postgressHost};Database={postgressDB};Username={postgressUser};Password={postgressPassword};{exstraParams}";
            bool parseResult = int.TryParse(Environment.GetEnvironmentVariable("JWT_EXPIRATION_MINUTES") ?? throw new InvalidOperationException("JWT_EXPIRATION_MINUTES не найден"), out jwtExpirationMinutes);
            if (!parseResult)
                throw new InvalidOperationException($"JWT_EXPIRATION_MINUTES имеет некорректное значение. Ожидается целое число.");


            builder.Services.AddDbContext<sportappContext>(options =>
                options.UseNpgsql(databaseConnectParams));
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
            builder.Services.AddMemoryCache();
            builder.Services.AddOpenApi();

            var app = builder.Build();

            if (app.Environment.IsDevelopment())
            {
                app.MapOpenApi();
            }

            app.UseHttpsRedirection();

            app.UseAuthentication();
            app.UseAuthorization();

            app.MapPost("/api/auth/register", async ([FromBody] LoginRecord request, sportappContext db, IMemoryCache cache) =>
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
                if(string.IsNullOrEmpty(request.EmailCode))
                {
                    errors["email"] = new[] { "Для подтверждения почты необходим код" };
                }
                string cacheKey = $"email_confirm_{request.Email}";
                string? codeHash = cache.Get<string>(cacheKey);
                if (string.IsNullOrEmpty(codeHash))
                {
                    errors["email"] = new[] { "Запроса на подтверждение этой почты не обнаружено" };
                }
                else if (!string.IsNullOrEmpty(request.EmailCode) && !BCrypt.Net.BCrypt.Verify(request.EmailCode, codeHash))
                {
                    errors["email"] = new[] { "Код неверный" };
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

                string jwt = GenerateJwtToken(newUser, secretKey, issuer, audience, jwtExpirationMinutes);
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

                string jwt = GenerateJwtToken(currentUser, secretKey, issuer, audience, jwtExpirationMinutes);
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
            app.MapPost("/api/auth/email-confirm", async ([FromBody] EmailConfirmRequest request, sportappContext db, IMemoryCache cache) =>
            {
                CheckResult emailCheck = Tools.CheckEmail(request.Email);
                CheckResult loginCheck = Tools.CheckLogin(request.Login);
                User? currentUser = null;
                var errors = new Dictionary<string, string[]>();
                if (emailCheck.error)
                {
                    errors["email"] = new[] { emailCheck.errorMessage };
                }
                else if (loginCheck.error)
                {
                    errors["login"] = new[] { loginCheck.errorMessage };
                }
                else
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Email == request.Email);
                }
                if (currentUser != null)
                    errors["email"] = new[] { "Почта уже подтверждена" };

                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                string cacheKey = $"email_confirm_{request.Email}";
                string code = Tools.GenerateCode();
                string codeHash = BCrypt.Net.BCrypt.HashPassword(code, workFactor: 12);
                EmailData emailData = new EmailData("Подтверждение электронного адреса", EmailGenerator.GenerateCodeHTML(request.Login, code));
                if (EmailSender.SendEmail(request.Login, request.Email, emailData, emailKey).Result)
                    cache.Set(cacheKey, codeHash, new MemoryCacheEntryOptions
                    {
                        AbsoluteExpirationRelativeToNow = TimeSpan.FromMinutes(15)
                    });
                else
                    return Results.InternalServerError();

                return Results.Ok();
            });

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

            
            
            app.Run();
        }
        
        private static string GenerateJwtToken(User user, string secretKey, string issuer, string audience, int jwtExpirationMinutes)
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
                expires: DateTime.UtcNow.AddMinutes(jwtExpirationMinutes),
                signingCredentials: credentials
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
