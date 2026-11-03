# Исправление ошибок сборки v1.21.1

В обновлении исправлены ошибки компиляции из `MainAppScreens.kt`:

1. Добавлен импорт `androidx.compose.runtime.saveable.rememberSaveable`. Без него компилятор не мог вывести типы состояний `tab`, `query`, `selectedFilter` и других переменных. Отсюда появлялись каскадные ошибки сравнения `MainTab` и вызовов `String.contains`.
2. В `app/build.gradle.kts` явно включена генерация `BuildConfig` (`buildFeatures.buildConfig = true`), так как приложение обращается к `com.sportakus.auth.BuildConfig.DEBUG` для предпросмотра административного интерфейса в debug-сборке.
3. Версия приложения повышена до `1.21.1-buildfix`, `versionCode = 23`.

## Проверка

ZIP-архив проверен на целостность. Полная Gradle-сборка в этой среде не выполнена: дистрибутив Gradle 8.0 отсутствует в локальном кеше, а wrapper должен загрузить его с `services.gradle.org`. После открытия проекта в Android Studio выполните **Sync Project with Gradle Files**, затем **Build → Rebuild Project**.
