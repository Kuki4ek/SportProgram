using MailKit.Security;
using MimeKit;
using sport_app_server.Models;

namespace sport_app_server
{
    public record EmailData(string Subject, string BodyText);
    public static class EmailSender
    {
        public static async Task<bool> SendEmail(string Nickname, string Email, EmailData emailData, string emailKey)
        {
            try
            {
                var message = new MimeMessage();
                message.From.Add(new MailboxAddress("Sport App", "notifi.sportakus@gmail.com"));
                message.To.Add(new MailboxAddress(Nickname, Email));
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
                return true;
            }
            catch (Exception ex)
            {
                return false;
            }
        }
        public static async Task<bool> SendEmail(User user, EmailData emailData, string emailKey)
        {
            return await SendEmail(user.Nickname, user.Email, emailData, emailKey);
        }
    }
    
}
