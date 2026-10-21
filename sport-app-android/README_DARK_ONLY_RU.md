# SportakusAuth — тёмная версия

Это отдельная сборка приложения, всегда использующая тёмную тему. Переключателя темы нет. `applicationId`: `com.sportakus.auth.dark`.

## Запуск

1. Распакуйте архив.
2. Откройте папку, содержащую `settings.gradle.kts`, в Android Studio.
3. Дождитесь Gradle Sync, выполните **Build → Rebuild Project**, затем **Run ▶**.

## Основные технологии

Kotlin, Jetpack Compose, Material 3, Gradle 8.0, Android Gradle Plugin 8.1.1, Kotlin 1.9.0, Compose Compiler 1.5.1, `compileSdk/targetSdk = 34`, `minSdk = 24`, JDK 17.

## Иконки Google и Яндекс

Файлы находятся в `app/src/main/res/drawable/google_logo.png` и `yandex_logo.png`. Чтобы применить изображения дизайнера, замените эти PNG, сохранив имена. Код подключения находится в `ui/AuthScreens.kt`, функция `SocialAuthButtons()`, ресурсы вызываются как `R.drawable.google_logo` и `R.drawable.yandex_logo`.

OAuth, отправка SMS/e-mail и API-авторизация пока не подключены. Экран входа и другие сценарии пока используют демонстрационные обработчики там, где нужен сервер.

Примечание: полную Gradle-сборку в среде подготовки подтвердить не удалось, потому что не получилось загрузить Gradle 8.0 с `services.gradle.org`.
