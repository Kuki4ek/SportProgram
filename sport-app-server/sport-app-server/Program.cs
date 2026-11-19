using DotNetEnv;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.IdentityModel.Tokens;
using sport_app_server.Models;
using System.IdentityModel.Tokens.Jwt;
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
    public record UserInfoChangeRequest(string? NewLogin, string? NewNickname, string? NewEmail, string? EmailCode, string? NewPhone, string? NewPassword, int? Weight, int? Height);
    public record EmailChangeRequest(string NewEmail);
    public record PartnerRequest(string Name, string Logo, string ContactName, string ContactEmail, string ContactPhone);
    public record TagsRequest(string Code, string Name);
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
                int findLogin = (from user in users where user.Login == request.Login select user).Count();
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
                if (currentUser != null)
                {
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
                }
                return Results.InternalServerError();
            });
            app.MapGet("/api/users/me", async (ClaimsPrincipal user, sportappContext db) =>
            {
                var userId = user.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var errors = new Dictionary<string, string[]>();
                Guid userGuid;
                if (userId == null || !Guid.TryParse(userId, out userGuid))
                    return Results.InternalServerError();
                User? currentUser = await db.Users.FirstOrDefaultAsync(user_ => user_.Id == userGuid);
                if (currentUser != null)
                {
                    return Results.Ok(new
                    {
                        login = currentUser.Login,
                        phone = currentUser.PhoneNumber,
                        email = currentUser.Email,
                        createdAt = currentUser.CreatedAt,
                        balance = currentUser.Balance,
                        daysStraight = currentUser.DaysStraight,
                        xpPoints = currentUser.XpPoints
                    });
                }
                else
                {
                    return Results.InternalServerError();
                }
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

                if (currentUser == null)
                    return Results.InternalServerError();

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

                if (currentUser == null)
                    return Results.InternalServerError();

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

                if (currentUser == null)
                    return Results.InternalServerError();

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
                RefreshToken? oldRefreshToken = await db.RefreshTokens.Include(rt => rt.User).FirstOrDefaultAsync(rt => rt.TokenHash == hashedToken && rt.Device == device);
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

                if (oldRefreshToken == null)
                    return Results.InternalServerError();

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
                var userId = user.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var errors = new Dictionary<string, string[]>();
                var users = await db.Users.ToListAsync();
                Guid userGuid;
                if (userId == null || !Guid.TryParse(userId, out userGuid))
                    return Results.InternalServerError();
                User? currentUser = await db.Users.FirstOrDefaultAsync(user_ => user_.Id == userGuid);
                string hashedToken = Tools.HashString(request.RefreshToken);
                RefreshToken? RefreshToken = await db.RefreshTokens.Include(rt => rt.User).FirstOrDefaultAsync(rt => rt.TokenHash == hashedToken);
                if (RefreshToken != null && currentUser != null && RefreshToken.RevokedAt == null && RefreshToken.UserId == currentUser.Id)
                {
                    RefreshToken.RevokedAt = DateTime.UtcNow;
                }
                await db.SaveChangesAsync();
                return Results.Ok();
            }).RequireAuthorization();
            app.MapPut("/api/users/me", async ([FromBody] UserInfoChangeRequest request, ClaimsPrincipal user, sportappContext db, IMemoryCache cache) =>
            {
                var userId = user.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var errors = new Dictionary<string, string[]>();
                var users = await db.Users.ToListAsync();
                Guid userGuid;
                if (userId == null || !Guid.TryParse(userId, out userGuid))
                    return Results.InternalServerError();
                User? currentUser = await db.Users.FirstOrDefaultAsync(user_ => user_.Id == userGuid);
                if (currentUser == null)
                {
                    return Results.InternalServerError();
                }
                if (!string.IsNullOrEmpty(request.NewLogin))
                {
                    CheckResult loginCheck = Tools.CheckLogin(request.NewLogin);
                    if (loginCheck.error)
                    {
                        errors["NewLogin"] = new[] { loginCheck.errorMessage };
                    }
                    else
                    {
                        int findLogin = (from findUser in users where findUser.Login == request.NewLogin select findUser).Count();
                        if (findLogin > 0)
                        {
                            errors["NewLogin"] = new[] { "Текущий логин уже занят" };
                        }
                    }
                }
                if (!string.IsNullOrEmpty(request.NewNickname))
                {
                    CheckResult nicknameCheck = Tools.CheckNickname(request.NewNickname);
                    if (nicknameCheck.error)
                    {
                        errors["NewNickname"] = new[] { nicknameCheck.errorMessage };
                    }
                }
                if (!string.IsNullOrEmpty(request.NewEmail))
                {
                    CheckResult emailCheck = Tools.CheckEmail(request.NewEmail);
                    if (emailCheck.error)
                    {
                        errors["NewEmail"] = new[] { emailCheck.errorMessage };
                    }
                    else
                    {
                        int findEmail = (from findUser in users where findUser.Email == request.NewEmail select findUser).Count();
                        if (findEmail > 0)
                        {
                            errors["NewEmail"] = new[] { "Текущая почта уже занята" };
                        }
                        else
                        {
                            string cacheKey = $"email_change_confirm_{request.NewEmail}";
                            string? codeHash = cache.Get<string>(cacheKey);
                            if (string.IsNullOrEmpty(codeHash))
                            {
                                errors["email"] = new[] { "Запроса на подтверждение этой почты не обнаружено" };
                            }
                            else if (string.IsNullOrEmpty(request.EmailCode) || !BCrypt.Net.BCrypt.Verify(request.EmailCode, codeHash))
                            {
                                errors["email"] = new[] { "Код неверный" };
                            }
                        }
                    }
                }
                if (!string.IsNullOrEmpty(request.NewPhone))
                {
                    CheckResult phoneCheck = Tools.CheckPhone(request.NewPhone);
                    if (phoneCheck.error)
                    {
                        errors["NewPhone"] = new[] { phoneCheck.errorMessage };
                    }
                    else
                    {
                        int findPhone = (from findUser in users where findUser.PhoneNumber == request.NewPhone select findUser).Count();
                        if (findPhone > 0)
                        {
                            errors["NewPhone"] = new[] { "Текущий номер уже занят" };
                        }
                    }
                }
                if (!string.IsNullOrEmpty(request.NewPassword))
                {
                    CheckResult passwordCheck = Tools.CheckPassword(request.NewPassword);
                    if (passwordCheck.error)
                    {
                        errors["NewPassword"] = new[] { passwordCheck.errorMessage };
                    }
                }
                if (request.Weight.HasValue)
                {
                    if (request.Weight.Value < 0 || request.Weight.Value > 700)
                    {
                        errors["Weight"] = new[] { "Вес находится в недопустимом диапазоне" };
                    }
                }
                if (request.Height.HasValue)
                {
                    if (request.Height.Value < 0 || request.Height.Value > 300)
                    {
                        errors["Height"] = new[] { "Рост находится в недопустимом диапазоне" };
                    }
                }

                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                if (!string.IsNullOrEmpty(request.NewLogin))
                {
                    currentUser.Login = request.NewLogin;
                }
                if (!string.IsNullOrEmpty(request.NewNickname))
                {
                    currentUser.Nickname = request.NewNickname;
                }
                if (!string.IsNullOrEmpty(request.NewEmail))
                {
                    currentUser.Email = request.NewEmail;
                    string cacheKey = $"email_change_confirm_{request.NewEmail}";
                    cache.Remove(cacheKey);
                }
                if (!string.IsNullOrEmpty(request.NewPhone))
                {
                    currentUser.PhoneNumber = request.NewPhone;
                }
                if (!string.IsNullOrEmpty(request.NewPassword))
                {
                    currentUser.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.NewPassword, workFactor: 12);
                }
                
                if (request.Weight.HasValue || request.Height.HasValue)
                {
                    List<UserBodyMetric> metrics = new List<UserBodyMetric>();
                    metrics = await db.UserBodyMetrics
                                        .Where(m => m.UserId == currentUser.Id)
                                        .OrderByDescending(m => m.RecordedAt)
                                        .ToListAsync();
                    UserBodyMetric newMetrics = new();
                    newMetrics.UserId = currentUser.Id;
                    if (metrics.Count > 0)
                    {
                        newMetrics.Height = metrics[0].Height;
                        newMetrics.Weight = metrics[0].Weight;
                    }
                    newMetrics.Height = request.Height.HasValue ? request.Height.Value : newMetrics.Height;
                    newMetrics.Weight = request.Weight.HasValue ? request.Weight.Value : newMetrics.Weight;
                    newMetrics.Source = "PROFILE";
                    newMetrics.RecordedAt = DateTime.UtcNow;

                    db.UserBodyMetrics.Add(newMetrics);
                }

                await db.SaveChangesAsync();
                return Results.Ok();
            }).RequireAuthorization();
            app.MapPost("/api/users/me/email-confirm", async ([FromBody] EmailChangeRequest request, ClaimsPrincipal user, sportappContext db, IMemoryCache cache) =>
            {
                var userId = user.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                var errors = new Dictionary<string, string[]>();
                var users = await db.Users.ToListAsync();
                Guid userGuid;
                if (userId == null || !Guid.TryParse(userId, out userGuid))
                    return Results.InternalServerError();
                User? currentUser = await db.Users.FirstOrDefaultAsync(user_ => user_.Id == userGuid);
                CheckResult emailCheck = Tools.CheckEmail(request.NewEmail);
                if (emailCheck.error)
                {
                    errors["email"] = new[] { emailCheck.errorMessage };
                }
                int findEmail = (from findUser in users where findUser.Email == request.NewEmail select findUser).Count();
                if (findEmail > 0)
                {
                    errors["NewEmail"] = new[] { "Текущая почта уже занята" };
                }

                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                if (currentUser == null)
                    return Results.InternalServerError();    
                string cacheKey = $"email_change_confirm_{request.NewEmail}";
                string code = Tools.GenerateCode();
                string codeHash = BCrypt.Net.BCrypt.HashPassword(code, workFactor: 12);
                EmailData emailData = new EmailData("Подтверждение смены электронного адреса", EmailGenerator.GenerateCodeHTML(currentUser.Login, code));
                if (EmailSender.SendEmail(currentUser.Login, request.NewEmail, emailData, emailKey).Result)
                    cache.Set(cacheKey, codeHash, new MemoryCacheEntryOptions
                    {
                        AbsoluteExpirationRelativeToNow = TimeSpan.FromMinutes(15)
                    });
                else
                    return Results.InternalServerError();

                return Results.Ok();
            }).RequireAuthorization();
            app.MapGet("/api/partners", async (sportappContext db) =>
            {
                List<Partner> partners = await db.Partners.ToListAsync();
                return Results.Ok(partners);
            }).RequireAuthorization();
            app.MapGet("/api/partners/{id:int}", async (int id, sportappContext db) =>
            {
                Partner? partner = await db.Partners.FirstOrDefaultAsync(p => p.Id == id);
                if (partner == null)
                    return Results.NotFound();
                return Results.Ok(partner);
            }).RequireAuthorization();
            app.MapPost("/api/partners", async (PartnerRequest request, sportappContext db) =>
            {
                CheckResult emailCheck = Tools.CheckEmail(request.ContactEmail);
                CheckResult phoneCheck = Tools.CheckPhone(request.ContactPhone);
                var errors = new Dictionary<string, string[]>();
                if (emailCheck.error == true)
                {
                    errors["EmailCheck"] = new[] { emailCheck.errorMessage };
                }
                if (phoneCheck.error == true)
                {
                    errors["PhoneCheck"] = new[] { phoneCheck.errorMessage };
                }
                if (string.IsNullOrEmpty(request.Name))
                {
                    errors["Name"] = new[] { "Имя партнёра обязательно" };
                }
                if (string.IsNullOrEmpty(request.Logo))
                {
                    errors["Logo"] = new[] { "Логитип партнёра обязателен" };
                }

                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                Partner partner = new Partner();
                partner.Name = request.Name;
                partner.Logo = request.Logo;
                partner.ContactName = request.ContactName;
                partner.ContactEmail = request.ContactEmail;
                partner.ContactPhone = request.ContactPhone;
                db.Partners.Add(partner);
                await db.SaveChangesAsync();
                return Results.Ok();
            }).RequireAuthorization(p => p.RequireRole("ADMIN"));
            app.MapPut("/api/partners/{id:int}", async (int id, PartnerRequest request, sportappContext db) =>
            {
                
                CheckResult emailCheck = Tools.CheckEmail(request.ContactEmail);
                CheckResult phoneCheck = Tools.CheckPhone(request.ContactPhone);
                var errors = new Dictionary<string, string[]>();
                if (emailCheck.error == true)
                {
                    errors["EmailCheck"] = new[] { emailCheck.errorMessage };
                }
                if (phoneCheck.error == true)
                {
                    errors["PhoneCheck"] = new[] { phoneCheck.errorMessage };
                }
                if (string.IsNullOrEmpty(request.Name))
                {
                    errors["Name"] = new[] { "Имя партнёра обязательно" };
                }
                if (string.IsNullOrEmpty(request.Logo))
                {
                    errors["Logo"] = new[] { "Логитип партнёра обязателен" };
                }
                Partner? currentPartner = await db.Partners.FirstOrDefaultAsync(p => p.Id == id);
                if (currentPartner == null)
                    return Results.NotFound();

                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                currentPartner.Name = request.Name;
                currentPartner.Logo = request.Logo;
                currentPartner.ContactName = request.ContactName;
                currentPartner.ContactEmail = request.ContactEmail;
                currentPartner.ContactPhone = request.ContactPhone;
                await db.SaveChangesAsync();
                return Results.Ok();
            }).RequireAuthorization(p => p.RequireRole("ADMIN"));
            app.MapDelete("/api/partners/{id:int}", async (int id, sportappContext db) =>
            {
                var partner = await db.Partners.FirstOrDefaultAsync(p => p.Id == id);
                if (partner == null)
                    return Results.NotFound();

                db.Partners.Remove(partner);
                await db.SaveChangesAsync();

                return Results.NoContent();
            }).RequireAuthorization(p => p.RequireRole("ADMIN"));
            app.MapPost("/api/tags", async ([FromBody] TagsRequest request, sportappContext db) =>
            {
                var errors = new Dictionary<string, string[]>();
                if (string.IsNullOrEmpty(request.Code) && request.Code.Length > 50)
                {
                    errors["Code"] = new[] { "Код некорректный" };
                }
                if(string.IsNullOrEmpty(request.Name) && request.Name.Length > 100)
                {
                    errors["Name"] = new[] { "Имя некорректно" };
                }
                if (errors.Count > 0)
                    return Results.ValidationProblem(errors);

                Tag newTag = new Tag();
                newTag.Code = request.Code;
                newTag.Name = request.Name;
                db.Tags.Add(newTag);
                await db.SaveChangesAsync();
                return Results.Ok();
            }).RequireAuthorization(p => p.RequireRole("ADMIN"));
            app.Run();
        }
        
        private static string GenerateJwtToken(User user, string secretKey, string issuer, string audience, int jwtExpirationMinutes)
        {
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Login),
                new Claim(ClaimTypes.Role, user.UserRole)
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
