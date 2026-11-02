
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
using System.Runtime.CompilerServices;
using System.Security.Claims;
using System.Text;

namespace sport_app_server
{
    public record LoginRecord(string Login, string Email, string Password, string? Phone, string? EmailCode);
    public record LoginAnswer(string RefreshToken);
    public record EmailConfirmRequest(string Login, string Email);
    public record ForgotPasswordRequest(string Email, string Phone, string Login);
    public record ForgotPasswordCodeRequest(string Email, string Phone, string Login, string CodeToken, string EmailCode);
    public record ForgotPasswordResetRequest(string Email, string Phone, string Login, string ResetPasswordToken, string NewPassword);
    public record CodeTokenAnswer(string CodeToken);
    public record ResetPasswordTokenAnswer(string ResetPasswordToken);
    public record RefreshTokenRequest(string RefreshToken);
    public record RefreshTokenAnswer(string JWT, string RefreshToken);
    public record LogoutRequest(string RefreshToken);
    public class Program
    {
        
        public static void Main(string[] args)
        {


            var builder = WebApplication.CreateBuilder(args);

            Env.Load("../.env");
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
            builder.Services.AddHealthChecks().AddNpgSql(databaseConnectParams);

            var app = builder.Build();

            if (app.Environment.IsDevelopment())
            {
                app.MapOpenApi();
            }

            app.UseHttpsRedirection();

            app.UseAuthentication();
            app.UseAuthorization();

            app.MapHealthChecks("/health");
            app.MapPost("/api/auth/register", async ([FromBody] LoginRecord request, HttpContext httpContext, sportappContext db, IMemoryCache cache) =>
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
                cache.Remove(cacheKey);
                User newUser = new User();
                newUser.Id = Guid.NewGuid();
                newUser.Login = request.Login;
                newUser.Balance = 0;
                newUser.XpPoints = 0;
                newUser.DaysStraight = 0;
                newUser.Email = request.Email;
                if (!string.IsNullOrEmpty(request.Phone))
                    newUser.PhoneNumber = request.Phone;
                newUser.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password, workFactor: 12);
                newUser.CreatedAt = DateTime.UtcNow;
                db.Users.Add(newUser);
                
                string refreshToken = Tools.GenerateToken(32);
                RefreshToken newRefreshToken = new RefreshToken
                {
                    Id = Guid.NewGuid(),
                    UserId = newUser.Id,
                    TokenHash = Tools.HashString(refreshToken),
                    ExpiresAt = DateTime.UtcNow.AddDays(30),
                    Device = httpContext.Request.Headers.UserAgent.ToString() ?? "unknown",
                    CreatedAt = DateTime.UtcNow,
                    Ip = httpContext.Connection.RemoteIpAddress,
                    // User = newUser
                };
                db.RefreshTokens.Add(newRefreshToken);
                await db.SaveChangesAsync();

                LoginAnswer loginAnswer = new LoginAnswer(refreshToken);
                return Results.Ok(loginAnswer);
            });
            app.MapPost("/api/auth/login", async ([FromBody] LoginRecord request, HttpContext httpContext, sportappContext db) =>
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
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Login == request.Login);
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

                string refreshToken = Tools.GenerateToken(32);
                RefreshToken newRefreshToken = new RefreshToken
                {
                    Id = Guid.NewGuid(),
                    UserId = currentUser.Id,
                    TokenHash = Tools.HashString(refreshToken),
                    ExpiresAt = DateTime.UtcNow.AddDays(30),
                    Device = httpContext.Request.Headers.UserAgent.ToString() ?? "unknown",
                    CreatedAt = DateTime.UtcNow,
                    Ip = httpContext.Connection.RemoteIpAddress,
                    User = currentUser
                };
                db.RefreshTokens.Add(newRefreshToken);
                await db.SaveChangesAsync();

                LoginAnswer loginAnswer = new LoginAnswer(refreshToken);
                return Results.Ok(loginAnswer);
            });
            app.MapGet("/api/users/me", async (ClaimsPrincipal user, sportappContext db) =>
            {
                var userLogin = user.FindFirst(ClaimTypes.Name)?.Value;
                User currentUser = await db.Users.FirstOrDefaultAsync(user => user.Login == userLogin);
                return Results.Ok(new
                {
                    login = currentUser.Login,
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
            app.MapPost("/api/auth/forgot-password/request", async ([FromBody] ForgotPasswordRequest request, sportappContext db, IMemoryCache cache) =>
            {
                var errors = new Dictionary<string, string[]>();
                CheckResult loginCheck = Tools.CheckLogin(request.Login);
                CheckResult emailCheck = Tools.CheckEmail(request.Email);
                CheckResult phoneCheck = Tools.CheckPhone(request.Phone);

                var users = await db.Users.ToListAsync();
                User? currentUser = null;
                if (!loginCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Login == request.Login);
                }
                else if (!emailCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Email == request.Email);
                }
                else if (!string.IsNullOrEmpty(request.Phone) && !phoneCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.PhoneNumber == request.Phone);
                }

                if (currentUser == null || string.IsNullOrEmpty(currentUser.PasswordHash))
                {
                    errors["authentification"] = new[] { "Данные введены не корректно" };
                }

                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                string token = Tools.GenerateToken(32);
                string cacheKey = $"code_token_{token}_{currentUser.Login}";
                string code = Tools.GenerateCode();
                string codeHash = BCrypt.Net.BCrypt.HashPassword(code, workFactor: 12);
                EmailData emailData = new EmailData("Подтверждение ", EmailGenerator.GenerateCodeHTML(currentUser.Login, code));
                if (EmailSender.SendEmail(currentUser.Login, currentUser.Email, emailData, emailKey).Result)
                    cache.Set(cacheKey, codeHash, new MemoryCacheEntryOptions
                    {
                        AbsoluteExpirationRelativeToNow = TimeSpan.FromMinutes(15)
                    });
                else
                    return Results.InternalServerError();
                CodeTokenAnswer passwordResetAnswer = new CodeTokenAnswer(token);
                return Results.Ok(passwordResetAnswer);
            });
            app.MapPost("/api/auth/forgot-password/code", async ([FromBody] ForgotPasswordCodeRequest request, sportappContext db, IMemoryCache cache) =>
            {
                var errors = new Dictionary<string, string[]>();
                CheckResult loginCheck = Tools.CheckLogin(request.Login);
                CheckResult emailCheck = Tools.CheckEmail(request.Email);
                CheckResult phoneCheck = Tools.CheckPhone(request.Phone);

                var users = await db.Users.ToListAsync();
                User? currentUser = null;
                if (!loginCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Login == request.Login);
                }
                else if (!emailCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Email == request.Email);
                }
                else if (!string.IsNullOrEmpty(request.Phone) && !phoneCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.PhoneNumber == request.Phone);
                }

                if (currentUser == null)
                {
                    errors["authentification"] = new[] { "Данные введены не корректно" };
                }

                if (string.IsNullOrEmpty(request.CodeToken))
                {
                    errors["CodeToken"] = new[] { "Для отправки кода необходим токен" };
                }

                if (string.IsNullOrEmpty(request.EmailCode))
                {
                    errors["EmailCode"] = new[] { "Необходим код подтверждения" };
                }
                if (currentUser != null)
                {
                    string cacheKey = $"code_token_{request.CodeToken}_{currentUser.Login}";
                    string? codeHash = cache.Get<string>(cacheKey);
                    if (string.IsNullOrEmpty(codeHash))
                    {
                        errors["password"] = new[] { "Запроса на смену пароля не обнаружено" };
                    }
                    else if (!string.IsNullOrEmpty(request.EmailCode) && !BCrypt.Net.BCrypt.Verify(request.EmailCode, codeHash))
                    {
                        errors["EmailCode"] = new[] { "Код неверный" };
                    }
                }
                else
                {
                    errors["user"] = new[] { "Ошибка" };
                }
                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                string resetToken = Tools.GenerateToken(32);
                string tokenHash = BCrypt.Net.BCrypt.HashPassword(resetToken, workFactor: 12);
                string resetTokenCacheKey = $"password_reset_{currentUser.Login}";
                cache.Remove($"code_token_{request.CodeToken}_{currentUser.Login}");
                cache.Set(resetTokenCacheKey, tokenHash, new MemoryCacheEntryOptions
                {
                    AbsoluteExpirationRelativeToNow = TimeSpan.FromMinutes(15)
                });
                Console.WriteLine($"resetTokenCacheKey: {resetTokenCacheKey}");
                ResetPasswordTokenAnswer answer = new ResetPasswordTokenAnswer(resetToken);
                return Results.Ok(answer);
            });
            app.MapPost("/api/auth/forgot-password/reset", async ([FromBody] ForgotPasswordResetRequest request, sportappContext db, IMemoryCache cache) =>
            {
                var errors = new Dictionary<string, string[]>();
                CheckResult loginCheck = Tools.CheckLogin(request.Login);
                CheckResult emailCheck = Tools.CheckEmail(request.Email);
                CheckResult phoneCheck = Tools.CheckPhone(request.Phone);
                CheckResult passwordCheck = Tools.CheckPassword(request.NewPassword);

                var users = await db.Users.ToListAsync();
                User? currentUser = null;
                if (!loginCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Login == request.Login);
                }
                else if (!emailCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.Email == request.Email);
                }
                else if (!string.IsNullOrEmpty(request.Phone) && !phoneCheck.error)
                {
                    currentUser = await db.Users.FirstOrDefaultAsync(user => user.PhoneNumber == request.Phone);
                }

                if(passwordCheck.error)
                {
                    errors["password"] = new[] { passwordCheck.errorMessage };
                }

                if (currentUser == null || string.IsNullOrEmpty(currentUser.PasswordHash))
                {
                    errors["authentification"] = new[] { "Данные введены не корректно" };
                }

                if (currentUser != null)
                {
                    string cacheKey = $"password_reset_{currentUser.Login}";
                    Console.WriteLine($"resetTokenCacheKey: {cacheKey}");
                    string? tokenHash = cache.Get<string>(cacheKey);
                    Console.WriteLine($"tokenHash: {tokenHash}");
                    if (string.IsNullOrEmpty(tokenHash))
                    {
                        errors["password"] = new[] { "Запроса на смену пароля не обнаружено" };
                    }
                    else if (!string.IsNullOrEmpty(request.ResetPasswordToken) && !BCrypt.Net.BCrypt.Verify(request.ResetPasswordToken, tokenHash))
                    {
                        errors["EmailCode"] = new[] { "Код неверный" };
                    }
                }

                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                currentUser.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.NewPassword, workFactor: 12);
                cache.Remove($"password_reset_{currentUser.Login}");
                await db.SaveChangesAsync();
                return Results.Ok();
            });
            app.MapPost("/api/auth/refresh", async([FromBody] RefreshTokenRequest request, HttpContext httpContext, sportappContext db) =>
            {
                var errors = new Dictionary<string, string[]>();

                if(string.IsNullOrEmpty(request.RefreshToken))
                {
                    errors["RefreshToken"] = new[] { "RefreshToken не может быть пустым" };
                }

                string hashedToken = Tools.HashString(request.RefreshToken);
                User? user = null;
                string device = httpContext.Request.Headers.UserAgent.ToString() ?? "unknown";
                RefreshToken oldRefreshToken = await db.RefreshTokens.Include(rt => rt.User).FirstOrDefaultAsync(rt => rt.TokenHash == hashedToken && rt.Device == device);
                if (oldRefreshToken == null)
                {
                    return Results.Unauthorized();
                }
                else if (oldRefreshToken.ExpiresAt < DateTime.UtcNow)
                {
                    return Results.Unauthorized();
                }
                else if (oldRefreshToken.RevokedAt != null)
                {
                    return Results.Unauthorized();
                }
                if (oldRefreshToken != null)
                {
                    user = oldRefreshToken.User;
                }
                if (user == null)
                {
                    return Results.Unauthorized();
                }
                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                string jwt = GenerateJwtToken(user, secretKey, issuer, audience, jwtExpirationMinutes);
                string newRefreshToken = Tools.GenerateToken(32);
                RefreshToken NewRefreshToken = new RefreshToken
                {
                    Id = Guid.NewGuid(),
                    UserId = user.Id,
                    TokenHash = Tools.HashString(newRefreshToken),
                    ExpiresAt = DateTime.UtcNow.AddDays(30),
                    Device = httpContext.Request.Headers.UserAgent.ToString() ?? "unknown",
                    CreatedAt = DateTime.UtcNow,
                    Ip = httpContext.Connection.RemoteIpAddress,
                    User = user,
                };
                oldRefreshToken.RevokedAt = DateTime.UtcNow;
                oldRefreshToken.ReplacedBy = NewRefreshToken.Id;
                db.RefreshTokens.Add(NewRefreshToken);
                await db.SaveChangesAsync();
                return Results.Ok(new RefreshTokenAnswer(jwt, newRefreshToken));
            });
            app.MapPost("/api/auth/logout", async ([FromBody] LogoutRequest request, ClaimsPrincipal user, sportappContext db) =>
            {
                string hashedToken = Tools.HashString(request.RefreshToken);
                RefreshToken RefreshToken = await db.RefreshTokens.Include(rt => rt.User).FirstOrDefaultAsync(rt => rt.TokenHash == hashedToken);
                var userLogin = user.FindFirst(ClaimTypes.Name)?.Value;
                User currentUser = await db.Users.FirstOrDefaultAsync(user => user.Login == userLogin);
                if (RefreshToken != null && currentUser != null && RefreshToken.RevokedAt == null && RefreshToken.UserId == currentUser.Id)
                {
                    RefreshToken.RevokedAt = DateTime.UtcNow;
                }
                await db.SaveChangesAsync();
                return Results.Ok();
            }).RequireAuthorization();
            app.Run();
        }
        
        private static string GenerateJwtToken(User user, string secretKey, string issuer, string audience, int jwtExpirationMinutes)
        {
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Login),
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
