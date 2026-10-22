namespace sport_app_server
{
    public static class EmailGenerator
    {
        public static string GenerateCodeHTML(string nickname, string code)
        {
            string templatePath = "codeTemplate.html";
            string htmlTemplate = File.ReadAllText(templatePath);
            htmlTemplate = htmlTemplate.Replace("{{NICKNAME}}", nickname).Replace("{{CODE}}", code);
            return htmlTemplate;
        }
    }
}
