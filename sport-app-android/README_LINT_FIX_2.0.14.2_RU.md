# SportakusAuth 2.0.14.2-light — исправление Android Lint

## Что изменено

- `android:windowLightNavigationBar` перенесён из общего ресурса `res/values/themes.xml` в `res/values-v26/themes.xml`, поскольку атрибут доступен начиная с API 26.
- `android:forceDarkAllowed` размещён только в `res/values-v29/themes.xml`, поскольку атрибут доступен начиная с API 29.
- В `values`, `values-v26` и `values-v29` сохранены остальные атрибуты одной и той же светлой темы, чтобы выбор квалифицированного ресурса не менял оформление.
- Сохранён программный вызов `window.decorView.setForceDarkAllowed(false)` в `MainActivity.kt` с проверкой `Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q`.
- Повышены `versionCode` до 41 и `versionName` до `2.0.14.2-light`.

## Совместимость

- API 24–25: базовая тема без атрибутов, появившихся в API 26 и API 29.
- API 26–28: добавляется `windowLightNavigationBar`.
- API 29+: добавляются оба атрибута — `windowLightNavigationBar` и `forceDarkAllowed`.

## Проверка

Исходный фрагмент CI сообщил о двух lint-ошибках, но показал только первую. Помимо явно указанного `forceDarkAllowed` в этом же файле находился `windowLightNavigationBar`, который также требует API 26 при `minSdk 24`; он также перенесён в ресурс с соответствующим qualifier.

Локальный запуск `:app:lintDebug` в этой среде не завершился: Gradle Wrapper не смог скачать Gradle 8.0 из-за недоступности `services.gradle.org` (UnknownHostException). Поэтому окончательный результат lint необходимо подтвердить повторным запуском CI.
