# Спортакус — интерфейс авторизации (Kotlin + Jetpack Compose)


## Структура проекта

```text
SportakusAuth/
├── app/
│   └── src/main/
│       ├── java/com/sportakus/auth/
│       │   ├── MainActivity.kt
│       │   └── ui/
│       │       ├── AuthApp.kt
│       │       ├── AuthScreens.kt
│       │       └── theme/
│       │           ├── Color.kt
│       │           └── Theme.kt
│       └── res/values/
├── build.gradle.kts
└── settings.gradle.kts
```

## Значки Госуслуг и ВКонтакте

Локальные файлы иконок находятся в `app/src/main/res/drawable/` (`gosuslugi_logo.png` и `vk_logo.png`). В `AuthScreens.kt` используется `import com.sportakus.auth.R`, поэтому ссылки `R.drawable.gosuslugi_logo` и `R.drawable.vk_logo` разрешаются при сборке. Иконки можно заменить другими PNG с теми же именами без изменения Kotlin-кода.
