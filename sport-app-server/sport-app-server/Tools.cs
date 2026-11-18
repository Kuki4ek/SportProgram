using System.Security.Cryptography;
using System.Text;

namespace sport_app_server
{
    public struct CheckResult
    {
        public bool error;
        public string errorMessage;
        public CheckResult()
        {
            error = false;
            errorMessage = "";
        }
    }
    public static class Tools
    {
        static string lettersChars = "abcdefghijklmnopqrstuvwxyz";
        static string numericChars = "0123456789";
        static string specialChars = "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~`";
        static string lowerBarChars = "_";
        public static string HashString(string stringToHash)
        {
            var bytes = SHA256.HashData(Encoding.UTF8.GetBytes(stringToHash));
            return Convert.ToBase64String(bytes);
        }
        public static bool CheckLatinNumericSpecial(string checkString, bool letterCheck = true, bool numericCheck = true, bool specialCheck = true, bool lowerBarCheck = true)
        {
            if (string.IsNullOrEmpty(checkString))
            {
                return false;
            }
            string lowerCheckString = checkString.ToLower();
            string commonChars = (letterCheck ? lettersChars : "") + (numericCheck ? numericChars : "") + (specialCheck ? specialChars : "") + (lowerBarCheck ? lowerBarChars : "");

            foreach (char checkChar in lowerCheckString)
            {
                if (!commonChars.Contains(checkChar))
                {
                    return false;
                }
            }
            return true;
        }
        public static bool ContainsChar(string source, string chars)
        {
            if (string.IsNullOrEmpty(chars) || string.IsNullOrEmpty(source))
                return false;
            foreach (char checkChar in source)
            {
                if (chars.Contains(checkChar))
                    return true;
            }
            return false;
        }
        public static CheckResult CheckLogin(string login)
        {
            CheckResult checkResult = new CheckResult();
            if (string.IsNullOrWhiteSpace(login))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Имя пользователя обязательно";
            }
            else if (login.Length > 50)
            {
                checkResult.error = true;
                checkResult.errorMessage = "Имя пользователя не должно быть длинее 50 символов";
            }
            else if (!CheckLatinNumericSpecial($"{login[0]}", numericCheck: false, specialCheck: false))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Имя пользователя должно начинаться с латинского символа или символа подчёркивания";
            }
            else if (!CheckLatinNumericSpecial(login, specialCheck: false))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Имя пользователя содержит недопустимые символы";
            }
            return checkResult;
        }
        public static CheckResult CheckNickname(string nickname)
        {
            CheckResult checkResult = new CheckResult();
            if (string.IsNullOrWhiteSpace(nickname))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Имя пользователя не корректно";
            }
            else if (nickname.Length > 50)
            {
                checkResult.error = true;
                checkResult.errorMessage = "Имя пользователя должно быть не длиннее 50 символов";
            }
            return checkResult;
        }
        public static CheckResult CheckPassword(string password)
        {
            CheckResult checkResult = new CheckResult();
            if (string.IsNullOrWhiteSpace(password))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Пароль обязателен";
            }
            else if (password.Length < 8)
            {
                checkResult.error = true;
                checkResult.errorMessage = "Длина пароля должна быть не менее 8 символов";
            }
            else if (!CheckLatinNumericSpecial(password))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Пароль содержит недопустимые символы";
            }
            else if (!ContainsChar(password, lettersChars))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Пароль должен содержать как минимум один строчный символ";
            }
            else if (!ContainsChar(password, lettersChars.ToUpper()))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Пароль должен содержать как минимум один заглавный символ";
            }
            else if (!ContainsChar(password, numericChars))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Пароль должен содержать как минимум одну цифру";
            }
            else if (!ContainsChar(password, specialChars))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Пароль должен содержать как минимум один специальный символ";
            }
            return checkResult;
        }
        public static CheckResult CheckEmail(string email)
        {
            CheckResult checkResult = new CheckResult();
            if (string.IsNullOrWhiteSpace(email))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Почта обязательна";
            }
            else if (email.Length > 100)
            {
                checkResult.error = true;
                checkResult.errorMessage = "Длина адреса почты должна быть меньше 100 символов";
            }
            else if (!email.Contains("@") || !email.Contains(".") || (email.LastIndexOf("@") > email.LastIndexOf(".")) || !((email.LastIndexOf(".") - email.LastIndexOf("@")) >= 2) || !((email.Length - email.LastIndexOf(".")) >= 2) || (email.LastIndexOf('@') == 0))
            {
                checkResult.error = true;
                checkResult.errorMessage = "Почта должна быть вида example@example.ru";
            }
            return checkResult;
        }
        public static CheckResult CheckPhone(string? phone)
        {
            CheckResult checkResult = new CheckResult();
            if (!string.IsNullOrEmpty(phone))
            {
                string[] partsPhone = phone.Split(' ');
                if (partsPhone.Length != 3)
                {
                    checkResult.error = true;
                }
                else if (
                    (partsPhone[0][0] != '+') ||
                    (partsPhone[0].Length < 2) ||
                    (partsPhone[0].Length > 5) ||
                    !(int.TryParse(partsPhone[0][1..], out int result0))
                  )
                {
                    checkResult.error = true;
                }
                else if (
                    (partsPhone[1].Length != 5) ||
                    (partsPhone[1][0] != '(') ||
                    (partsPhone[1][4] != ')') ||
                    !(int.TryParse(partsPhone[1][1..^1], out int result1))
                )
                {
                    checkResult.error = true;
                }
                else if ((partsPhone[2].Length != 9))
                {
                    string[] lastPartPatrs = partsPhone[2].Split('-');
                    if (lastPartPatrs.Length != 3 ||
                        lastPartPatrs[0].Length != 3 ||
                        lastPartPatrs[1].Length != 2 ||
                        lastPartPatrs[2].Length != 2 ||
                        !(int.TryParse(lastPartPatrs[0], out int result20)) ||
                        !(int.TryParse(lastPartPatrs[1], out int result21)) ||
                        !(int.TryParse(lastPartPatrs[2], out int result22))
                        )
                    {
                        checkResult.error = true;
                    }
                }
                if (checkResult.error == true)
                {
                    checkResult.errorMessage = "Телефон должен быть вида +X (XXX) XXX-XX-XX";
                }
            }
            return checkResult;
        }
        public static string GenerateCode(int digits = 6)
        {
            return RandomNumberGenerator.GetInt32((int)Math.Pow(10,(digits - 1)), (int)Math.Pow(10, digits)).ToString();
        }
        public static string GenerateToken(int length)
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(length);
            string charSet = lettersChars.ToUpper() + lettersChars + numericChars + specialChars;
            StringBuilder token = new StringBuilder();
            for (int i = 0; i < length; i++)
            {
                token.Append(charSet[RandomNumberGenerator.GetInt32(charSet.Length)]);
            }
            return token.ToString();
        }
    }
}
