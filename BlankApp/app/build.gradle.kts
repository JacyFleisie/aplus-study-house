import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    // Static analysis (restored — runs manually via `detekt` / `ktlintCheck`)
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

// Load keystore properties from gitignored file
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// Load Supabase credentials from gitignored file
val supabasePropertiesFile = rootProject.file("supabase.properties")
val supabaseProperties = Properties()
if (supabasePropertiesFile.exists()) {
    supabaseProperties.load(FileInputStream(supabasePropertiesFile))
}

android {
    namespace = "com.example.blankapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.blankapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 18
        versionName = "1.3.3"

        buildConfigField("String", "VERSION_NAME", "\"1.3.3\"")
        buildConfigField("int", "VERSION_CODE", "18")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = if (keystorePropertiesFile.exists()) {
                file(keystoreProperties.getProperty("storeFile", "../aplus-study-house.jks"))
            } else {
                file("../aplus-study-house.jks")
            }
            storePassword = keystoreProperties.getProperty("storePassword", "")
            keyAlias = keystoreProperties.getProperty("keyAlias", "")
            keyPassword = keystoreProperties.getProperty("keyPassword", "")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${supabaseProperties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${supabaseProperties.getProperty("SUPABASE_ANON_KEY", "")}\"")
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    lint {
        abortOnError = false
        xmlReport = true
        htmlReport = true
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    testImplementation(composeBom)

    // Compose UI & Material 3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material") // for PullRefreshIndicator (pull-to-refresh)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.2")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // HTTP client for Supabase REST API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Encrypted SharedPreferences (secure token storage)
    implementation("androidx.security:security-crypto:1.0.0")



    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.json:json:20231013")
    testImplementation("com.google.dagger:hilt-android-testing:2.50")
    kspTest("com.google.dagger:hilt-compiler:2.50")

    // Android Instrumentation Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}

// ============================================
// STATIC ANALYSIS (restored)
// Runs on demand: ./gradlew detekt ktlintCheck
// Deliberately NOT wired into `build`/`assembleRelease` so quality gates
// can never block producing the release APK.
// ============================================

detekt {
    buildUponDefaultConfig = true
    allRules = false
    // Generate a baseline so pre-existing issues don't fail a first run.
    baseline = file("detekt-baseline.xml")
    config.setFrom("$rootDir/config/detekt/detekt.yml")
}

ktlint {
    // Android Kotlin style guide + Compose rules
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
