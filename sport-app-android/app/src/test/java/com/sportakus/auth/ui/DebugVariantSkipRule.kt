package com.sportakus.auth.ui

import org.junit.Assume.assumeTrue
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Правило для release-варианта юнит-тестов.
 *
 * ComponentActivity, на которой работает createComposeRule(), регистрируется
 * манифест-артефактом androidx.compose.ui:ui-test-manifest, который подключён
 * как debugImplementation. Правило AndroidComposeTestRule запускает Activity
 * из своего statement — ещё ДО @Before, — поэтому проверку варианта нельзя
 * выполнять в @Before: в release каждый тест упадёт с RuntimeException при
 * старте Activity (след в логе CI — «RuntimeException at
 * RoboMonitoringInstrumentation.java:105»). Это правило подменяет
 * Compose-правило целиком и помечает все тесты класса как пропущенные.
 */
class DebugVariantSkipRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                assumeTrue(
                    "Compose-UI-тесты выполняются только в debug-варианте (ui-test-manifest)",
                    false
                )
            }
        }
}
