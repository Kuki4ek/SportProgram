package com.sportakus.auth

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.sportakus.auth.ui.AuthApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Defensive runtime opt-out: Android 10/11 Force Dark must not alter this light-only UI.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.setForceDarkAllowed(false)
        }

        setContent {
            AuthApp()
        }
    }
}
