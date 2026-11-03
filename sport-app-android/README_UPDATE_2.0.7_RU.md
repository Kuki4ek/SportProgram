# SportakusAuth V2.0.7-light — обновление API

## Что изменено

- `POST /api/auth/register` отправляет `login`, `email`, `emailCode`, `password` и необязательный `phone`; после успешного ответа получает `refreshToken` и автоматически обменивает его через `/api/auth/refresh` на `jwt` + новый `refreshToken`.
- `POST /api/auth/login` отправляет ровно одно из полей `login`, `email`, `phone`; после успешного ответа также выполняет refresh-обмен.
- `POST /api/auth/refresh` отправляет `{ "refreshToken": "..." }`, читает `jwt` и новый `refreshToken`.
- При 401 на защищённых запросах выполняется refresh и один повтор исходного запроса. Добавлена защита от одновременной повторной отправки одного и того же refresh-токена.
- `POST /api/auth/forgot-password/request` принимает одно из `login` / `email` / `phone` и читает `codeToken`.
- `POST /api/auth/forgot-password/code` отправляет идентификатор, `codeToken`, `emailCode` и читает `resetPasswordToken`.
- `POST /api/auth/forgot-password/reset` отправляет идентификатор, `resetPasswordToken`, `newPassword`; пустое тело успешного ответа допускается.
- `POST /api/auth/logout` отправляет JWT в Bearer-заголовке и `refreshToken` в JSON.
- Если регистрация вернула 2xx, но refresh-обмен не завершился, приложение переводит пользователя на экран входа и не предлагает повторять регистрацию.
- Версия: `2.0.7-light`, `versionCode = 31`.

## Обязательное условие на стороне backend

Успешные ответы `/register` и `/login` должны содержать `refreshToken`. `/refresh` должен принимать его и возвращать оба поля `jwt` и новый `refreshToken`. Если сервер этого не делает, клиент не может сам создать или вычислить JWT — backend необходимо привести к этому контракту.
