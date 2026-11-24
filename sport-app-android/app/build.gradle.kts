plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sportakus.auth"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sportakus.auth.v2"
        minSdk = 24
        targetSdk = 34
        versionCode = 42
        versionName = "2.0.14.3-light"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        // AuthApp and MainAppScreens use BuildConfig.DEBUG for debug-only admin preview.
        buildConfig = true
    }

    composeOptions {
        // Compose Compiler 1.5.1 is compatible with Kotlin 1.9.0.
        kotlinCompilerExtensionVersion = "1.5.1"
    }
	
	testOptions {
		unitTests {
			isIncludeAndroidResources = true
    }
}
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.googlecode.libphonenumber:libphonenumber:9.0.41")

    debugImplementation("androidx.compose.ui:ui-tooling")
	
	// JUnit 4
	testImplementation("junit:junit:4.13.2")

	// Robolectric
	testImplementation("org.robolectric:robolectric:4.11.1")

	// MockWebServer
	testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")

	// AndroidX Test
	testImplementation("androidx.test:core-ktx:1.5.0")
	testImplementation("androidx.test.ext:junit:1.1.5")

	// Compose UI Testing
	testImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
	testImplementation("androidx.compose.ui:ui-test-junit4")

	// Compose UI Test Manifest
	debugImplementation("androidx.compose.ui:ui-test-manifest")
}
