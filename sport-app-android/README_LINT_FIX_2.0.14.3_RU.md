# SportakusAuth 2.0.14.3-light — исправление Android Lint

## Что изменено

- `android:windowLightNavigationBar` размещён в `res/values-v27/themes.xml`: Android Lint подтвердил, что атрибут требует API 27, а не API 26.
- `android:forceDarkAllowed` остаётся только в `res/values-v29/themes.xml`, поскольку атрибут доступен начиная с API 29.
- Общая тема в `res/values/themes.xml` не содержит атрибутов, недоступных на минимальной версии Android (API 24).
- В каталогах `values`, `values-v27` и `values-v29` сохранены согласованные настройки светлой темы.
- Сохранён программный вызов `window.decorView.setForceDarkAllowed(false)` в `MainActivity.kt` с проверкой `Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q`.
- Повышены `versionCode` до 42 и `versionName` до `2.0.14.3-light`.

## Совместимость

- API 24–26: используется базовая тема без `windowLightNavigationBar` и `forceDarkAllowed`.
- API 27–28: дополнительно применяется `windowLightNavigationBar`.
- API 29 и выше: применяются оба атрибута.

## Результат предыдущего CI

Предыдущий запуск `./gradlew --no-daemon lint` успешно выполнил компиляцию Kotlin и Java, но завершился на одной lint-ошибке: `android:windowLightNavigationBar requires API level 27 (current min is 24)`. Исправление — перенос ресурса из `values-v26` в `values-v27`. В логе также присутствуют 23 предупреждения о неиспользуемых параметрах темы; они не являлись причиной остановки текущего запуска.

Полный Gradle Lint для этой версии необходимо подтвердить повторным запуском CI.
