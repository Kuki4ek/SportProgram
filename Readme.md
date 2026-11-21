# Спортакус

## Основная информация

Это приложение для получения полезных наград за тренировки.

Архитектура: клиент-серверная (мобильный клиент и монолитный бэкенд)

Стек клиента: Kotlin + Jetpack Compose

Стек серверной части: .NET 10 (C# + ASP)

## Структура репозитория

```text
SportProgram/
├── 🗂️ .github/                      <--- Настройки CI
│   ├── 🗂️ actions/                  <--- Кастомные GitHub Actions
│   │   └── 🗂️ setup-android/        <--- Установка JDK 17 и Android SDK
│   └── 🗂️ workflows/                <--- Пайплайны CI/CD
├── 🗂️ docs/                         <--- Документация
│   └── 📄️ API.md                    <--- Описание REST API
├── 🗂️ sport-app-android/            <--- Проект Android-клиента
│   ├── 🗂️ app/                      <--- Модуль приложения (UI, сеть, тесты)
│   ├── 🗂️ gradle/                   <--- Gradle Wrapper
│   ├── 📄️ build.gradle.kts          <--- Конфигурация сборки
│   └── 📄️ settings.gradle.kts       <--- Настройки Gradle-проекта
├── 🗂️ sport-app-server/             <--- Проект серверной части приложения
│   ├── 📄️ compose.yaml              <--- Docker Compose (БД + API)
│   └── 🗂️ sport-app-server/         <--- Исходный код серверной части
│       ├── 🗂️ Models/               <--- Модели сущностей БД
│       ├── 🗂️ Properties/           <--- Параметры запуска
│       ├── 📄️ Program.cs            <--- Точка входа и REST API
│       └── 📄️ Dockerfile            <--- Образ для сборки и публикации
├── 🗂️ test_data/                    <--- Тестовые данные
│   └── 📄️ test.sql                  <--- Скрипты наполнения БД
├── 📄️ schema.sql                    <--- Схема базы данных
└── 📄️ Readme.md
```
