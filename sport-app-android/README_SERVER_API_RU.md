# Sportakus Android V2 — контракт интеграции C# API

Эта версия клиента выровнена по техническому описанию backend от 02.11. Ниже перечислены DTO, которые Android отправляет и ожидает.

## Регистрация и подтверждение почты

1. Клиент проверяет поля локально: `login` обязателен; `email` должен иметь стандартный формат; пароль — **не менее 8 символов**; `phone` необязателен.
2. Отправка кода: `POST /api/auth/email-confirm`.

```json
{
  "login": "Alice",
  "email": "alice@example.com"
}
```

3. После ввода кода создание аккаунта: `POST /api/auth/register`.

```json
{
  "login": "Alice",
  "email": "alice@example.com",
  "emailCode": "123456",
  "password": "strongPassword123",
  "phone": "+7 (999) 999-99-99"
}
```

`phone` исключается из JSON, если поле не заполнено. Имя поля кода — строго `emailCode`, не `code`.

## Вход и получение JWT

`POST /api/auth/login` отправляет ровно одно поле идентификатора:

```json
{ "login": "Alice", "password": "strongPassword123" }
```

или

```json
{ "email": "alice@example.com", "password": "strongPassword123" }
```

или

```json
{ "phone": "+7 (999) 123-45-67", "password": "strongPassword123" }
```

**Важно: `register` и `login` не должны возвращать access token/JWT. По новой схеме оба успешных endpoint возвращают `refreshToken`:**

```json
{ "refreshToken": "32CharRefreshToken" }
```

После этого Android автоматически вызывает `POST /api/auth/refresh`:

```json
{ "refreshToken": "32CharRefreshToken" }
```

**Ожидаемый ответ `/api/auth/refresh`:**

```json
{
  "jwt": "ThisIsJWT",
  "refreshToken": "NewRefreshToken"
}
```

Клиент считает авторизацию завершённой только после получения обоих непустых полей `jwt` и `refreshToken`. Оба токена сохраняются в `TokenStore`; при каждом успешном refresh сохраняется ротированный `refreshToken`.

## Обновление сессии и повтор запросов

Для защищённых запросов Android передаёт:

```http
Authorization: Bearer <jwt>
```

При HTTP `401 Unauthorized` клиент один раз вызывает `POST /api/auth/refresh` с текущим `refreshToken`, сохраняет `jwt` и новый `refreshToken`, затем повторяет исходный запрос. Если `/api/auth/refresh` вернул `401`, локальная сессия очищается и пользователь должен войти заново. Для одновременных `401` используется защита от параллельного повторного расходования одного refresh-токена.

Сейчас в клиенте этот механизм применяется к авторизованным endpoint, которые уже подключены в Android: `GET /api/users/me`, `PUT /api/users/me` и `POST /api/auth/logout`. Новые защищённые endpoint нужно вызывать через тот же механизм `requestAuthorized`.

## Восстановление пароля

### 1. Запрос кода

`POST /api/auth/forgot-password/request` — в запросе ровно одно поле идентификатора:

```json
{ "email": "alice@example.com" }
```

Вместо `email` может быть `login` или `phone`.

Ответ:

```json
{ "codeToken": "myCodeToken" }
```

### 2. Проверка кода

`POST /api/auth/forgot-password/code`:

```json
{
  "email": "alice@example.com",
  "codeToken": "myCodeToken",
  "emailCode": "536093"
}
```

Вместо `email` может быть одно поле `login` или `phone`.

Ответ:

```json
{ "resetPasswordToken": "meResetPasswordToken" }
```

### 3. Установка нового пароля

`POST /api/auth/forgot-password/reset`:

```json
{
  "email": "alice@example.com",
  "resetPasswordToken": "meResetPasswordToken",
  "newPassword": "newPassword"
}
```

Вместо `email` может быть одно поле `login` или `phone`. Пароль должен содержать не менее 8 символов. Клиент принимает пустое тело успешного ответа.

## Logout

`POST /api/auth/logout` отправляется с JWT в заголовке и refresh-токеном в теле:

```http
Authorization: Bearer <jwt>
Content-Type: application/json
```

```json
{ "refreshToken": "32CharRefreshToken" }
```

Успешный ответ может быть пустым. После запроса локальные токены удаляются.

## Что backend обязан обеспечить

Для работы после регистрации/входа без ошибки получения access token сервер должен:

1. Возвращать HTTP 2xx и `{ "refreshToken": "..." }` из `/api/auth/register` и `/api/auth/login`.
2. Принимать `{ "refreshToken": "..." }` в `/api/auth/refresh`.
3. Возвращать из `/api/auth/refresh` оба поля: `jwt` и новый `refreshToken`.
4. Возвращать HTTP 401 из `/api/auth/refresh`, если refresh-токен недействителен/истёк.
5. Принимать logout с JWT в заголовке `Authorization: Bearer ...` и `refreshToken` в JSON.

Если сервер уже реализует именно эти ответы, менять backend не требуется. Если `/register` или `/login` не возвращает refreshToken, либо `/refresh` не возвращает jwt, соответствующую часть backend нужно исправить: получить JWT только на стороне Android без серверной выдачи невозможно.

## Профиль и партнёры — контракт от 18.11

Все endpoints этого раздела требуют JWT в заголовке `Authorization: Bearer ...`.

### Частичное изменение профиля

`PUT /api/users/me` отправляет только изменённые непустые поля: `newLogin`, `newNickname`, `newEmail`, `emailCode`, `newPhone`, `newPassword`, `weight`, `height`. Неизменённые поля не включаются в JSON. Для смены почты предварительно выполняется `POST /api/users/me/email-confirm` с `{ "newEmail": "..." }`; затем пользователь вводит код, и `newEmail` + `emailCode` отправляются вместе через PUT. Вес и рост — целые числа в пределах 0–700 и 0–300.

Поле `fullName` в спецификации 18.11 отсутствует, поэтому клиент не отправляет его на backend: оно остаётся локальным в SharedPreferences.

### Партнёры

- `GET /api/partners` — список;
- `GET /api/partners/{id}` — данные одного партнёра;
- `POST /api/partners` — создание, только `ADMIN`;
- `PUT /api/partners/{id}` — полная замена данных, только `ADMIN`;
- `DELETE /api/partners/{id}` — удаление, только `ADMIN`.

Поля партнёра: `id`, `name`, `logo`, `contactName`, `contactEmail`, `contactPhone`. Для создания/изменения клиент передаёт `name`, `logo`, `contactName`, `contactEmail`, `contactPhone`.

### Ошибки валидации

При `400 application/problem+json` ошибки извлекаются из `errors` как массивы сообщений. Исходный регистр ключей сохраняется: `NewLogin`, `NewNickname`, `NewEmail`, `NewPhone`, `NewPassword`, `Weight`, `Height`, `email`. HTTP 403 означает отсутствие роли `ADMIN`, HTTP 404 — отсутствие запрошенной записи.

## Настройка адреса сервера

В приложении открой «Адрес сервера» и укажи базовый URL без `/api/...`:

- Android Emulator: обычно `http://10.0.2.2:5000`;
- физический телефон в той же Wi-Fi сети: `http://<LAN-IP-компьютера>:<порт>`;
- другая сеть: публичный HTTPS URL или HTTPS-туннель.

Для production используй HTTPS и защищённое хранилище токенов на основе Android Keystore.

## Валидация логина и пароля (обновление от 05.11)

Клиент предварительно проверяет те же ограничения, которые применяет backend. Серверная валидация остаётся окончательной.

Логин при регистрации:
- обязателен;
- начинается с латинской буквы `A-Z` / `a-z` или `_`;
- далее содержит только латинские буквы, цифры и `_`;
- длина — не более 50 символов.

Пароль при регистрации и восстановлении:
- обязателен, длина не менее 8 символов;
- разрешены только ASCII-латиница, цифры и символы `!\"#$%&'()*+,-./:;<=>?@[]^{|}~`;
- обязательно присутствуют хотя бы одна заглавная буква, одна строчная буква, одна цифра и один символ из указанного набора спецсимволов.

На экране входа пароль проверяется на непустое значение, но новые требования сложности не блокируют вход для ранее созданных аккаунтов.

## Изменение полного имени профиля

Поле `fullName` теперь редактируется и сохраняется на сервере через существующий `PUT /api/users/me`. Остальные атрибуты профиля остаются только для чтения в интерфейсе. После успешного PUT клиент перечитывает профиль через `GET /api/users/me`. Этот пользовательский DTO не описан в auth-спецификации от 02.11, поэтому имя поля `fullName` и контракт `/api/users/me` должны соответствовать реализации профильного API backend.


## Клиентская реализация обновления 18.11

В `V2.0.14-light` добавлены профильные методы `requestCurrentUserEmailConfirmation` и `updateCurrentUserProfile`, а также методы списка, просмотра, создания, изменения и удаления партнёров. Все эти запросы используют общий механизм refresh + один повтор после 401. В UI добавлены подтверждение новой почты в редакторе профиля и раздел партнёров; административные действия доступны только при наличии ADMIN claim в JWT, а серверная проверка 403 остаётся окончательной.
