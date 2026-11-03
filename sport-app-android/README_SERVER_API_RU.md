# Интеграция Sportakus Android V2 с C# API

## Регистрация и подтверждение почты

1. Клиент проверяет данные формы локально.
2. Для отправки кода вызывает `POST /api/auth/email-confirm`:

```json
{
  "login": "Alice",
  "email": "alice@example.com"
}
```

3. После ввода шестизначного кода вызывает `POST /api/auth/register`:

```json
{
  "login": "Alice",
  "email": "alice@example.com",
  "emailCode": "123456",
  "password": "strongPassword123",
  "phone": "+7 (999) 999-99-99"
}
```

`phone` необязателен и не добавляется в JSON, если поле пустое. Название поля кода строго `emailCode`, не `code`.

## Вход

`POST /api/auth/login`. Клиент отправляет ровно одно поле идентификатора:

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

## Токены

Успешные ответы входа и регистрации должны содержать краткоживущий access token и долгоживущий refresh token, например:

```json
{
  "accessToken": "<access-token>",
  "refreshToken": "<refresh-token>"
}
```

Клиент также распознаёт совместимые варианты `access_token`, `refresh_token` и старое поле `jwt`. Оба токена хранятся в `TokenStore`; `getJwt()` оставлен как совместимый псевдоним для получения access token.

Обновление сессии реализовано для `GET /api/users/me`: при `401` клиент вызывает `POST /api/auth/refresh` с `{ "refreshToken": "..." }`, сохраняет новые токены и один раз повторяет исходный запрос. Выход вызывает `POST /api/auth/logout` с заголовком `Authorization: Bearer <accessToken>` и необязательным `refreshToken` в JSON. Локальные токены удаляются независимо от результата сетевого logout.

## Восстановление пароля

Клиент поддерживает три типа идентификатора: почта, телефон и nickname. Текущие маршрутные значения вынесены в `AuthEndpoints` в `data/api/AuthApi.kt`:

| Операция | Маршрут по умолчанию | JSON |
|---|---|---|
| Запрос кода | `POST /api/auth/forgot-password/request` | одно из полей `email`, `phone`, `nickname` |
| Проверка кода | `POST /api/auth/forgot-password/code` | `code_token`, `verify_code` |
| Установка нового пароля | `POST /api/auth/forgot-password/reset` | `code_token`, `verify_code`, `new_password`; если сервер вернул `reset_token`, он также передаётся |

Первый ответ ожидается с полем `code_token`; ответ проверки может дополнительно содержать `reset_token` (поддерживаются camelCase-варианты). Данные `code_token` и `verify_code` передаются и в финальном запросе смены пароля, чтобы поддержать серверный контракт, в котором повторная проверка выполняется на последнем шаге. Токены восстановления существуют только в состоянии приложения и не записываются в постоянное хранилище.

**Важно:** в присланном архиве и доступной части технического описания от 02.11 точные URL и DTO для трёх операций восстановления, refresh и logout не были приведены полностью. Поэтому значения этих маршрутов и полей — текущие согласованные предположения клиента, а не подтверждённый серверный контракт. Если backend использует другие имена, достаточно изменить `AuthEndpoints` и соответствующие JSON-ключи в `AuthApi`.

## Профиль

После входа/регистрации тестовый экран может вызывать:

```http
GET /api/users/me
Authorization: Bearer <accessToken>
Accept: application/json
```

Клиент извлекает личные данные из объекта ответа или вложенных `data`, `user`, `profile`. Поддерживаются поля `login`/`nickname`/`username`, `fullName`/`name`, `email`, `phone`, `birthDate`, `height`, `weight`, `gender`. Для статистики распознаются `totalWorkouts`, `sportCoins`/`balance`, `totalMinutes`, `favoriteCount` и массивы `activityValues`/`weeklyActivity` (включая snake_case варианты). Поля, которых нет в ответе, остаются пустыми или равными нулю; клиент не придумывает пользовательскую статистику.

Для телефона российский номер форматируется во время ввода и отправляется в формате `+7 (999) 999-99-99`. Этот формат используется также в полях входа, восстановления и редактирования профиля.

Сохранение редактируемого профиля пока предполагает `PUT /api/users/me` с полями `login`, `email`, `phone`, `fullName`, `birthDate`, `height`, `weight`, `gender`. Этот контракт не был подтверждён в предоставленном описании backend; перед реальным использованием согласуйте маршрут и DTO с C# разработчиком. Путь вынесен в `AuthEndpoints.UPDATE_CURRENT_USER`.

## Настройка адреса

В приложении откройте «Адрес сервера» и задайте базовый URL без `/api/...`:

- Android Emulator: обычно `http://10.0.2.2:5000`;
- физический телефон в той же Wi-Fi сети: `http://<LAN-IP-компьютера>:<порт>`;
- другая сеть: публичный HTTPS URL или HTTPS-туннель.

`http://` разрешён в этой учебной сборке для локального тестирования. Для реального размещения используйте HTTPS и защищённое хранилище токенов на основе Android Keystore.
