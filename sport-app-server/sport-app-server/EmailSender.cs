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
using DotNetEnv;
namespace sport_app_server
{
    public record EmailData(string Subject, string BodyText);
    public static class EmailSender
    {
        //-----------------Тестовая часть для отправки email-----------------------------------------------------
        public static async void SendEmail(User user, EmailData emailData, string emailKey)
        {
            try
            {
                var message = new MimeMessage();
                message.From.Add(new MailboxAddress("Sport App", "notifi.sportakus@gmail.com"));
                message.To.Add(new MailboxAddress(user.Nickname, user.Email));
                message.Subject = emailData.Subject;

                message.Body = new TextPart("html")
                {
                    Text = emailData.BodyText
                };

                using var client = new MailKit.Net.Smtp.SmtpClient();

                await client.ConnectAsync("smtp.gmail.com", 587, SecureSocketOptions.StartTls);

                await client.AuthenticateAsync("notifi.sportakus@gmail.com", emailKey);

                await client.SendAsync(message);
                await client.DisconnectAsync(true);
                
            }
            catch (Exception ex)
            {
                
            }
        }
    }
}
