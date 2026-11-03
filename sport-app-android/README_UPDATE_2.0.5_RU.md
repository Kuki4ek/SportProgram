# SportakusAuth V2.0.5-light — исправление компиляции

В версии 2.0.5 исправлены ошибки компиляции из предыдущей сборки:

- `MainAppScreens.kt`: добавлены явные импорты используемых Material Icons из `androidx.compose.material.icons.outlined` (`ArrowBack`, `Search`, `PlayCircleOutline`, `Home`, `Description`, `CheckCircle`).
- `OnboardingScreens.kt`: добавлен импорт `androidx.compose.foundation.layout.PaddingValues`.
- Обновлена версия приложения: `versionCode = 29`, `versionName = 2.0.5-light`.

Зависимость `androidx.compose.material:material-icons-extended` уже присутствовала в проекте, поэтому менять набор зависимостей для этих иконок не потребовалось.

Откройте проект в Android Studio и выполните **Build → Rebuild Project**.
