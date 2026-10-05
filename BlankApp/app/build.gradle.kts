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

// ============================================
// VERSION GUARD — fails the build if the app version does not increase
// past the latest v* release tag. Prevents a repeat of the v1.6.8-v1.6.11
// drift where every tagged build shipped versionName 1.6.7 and the
// self-updater nagged users forever.
// ============================================
tasks.register("checkVersionBump") {
    doLast {
        // Falls back to the classic `versionCode = N` line for tags that
        // predate the single-source appVersionCode variable.
        fun parseVersionCodeFallback(output: String): Int? {
            val fallback = Regex("versionCode\\s*=\\s*(\\d+)")
            return fallback.find(output)?.groupValues?.get(1)?.toIntOrNull()
        }

        fun parseVersionCode(output: String): Int? {
            // Matches the versionCode line in this file as of any commit
            val regex = Regex("val appVersionCode\\s*=\\s*(\\d+)")
            return regex.find(output)?.groupValues?.get(1)?.toIntOrNull()
                ?: parseVersionCodeFallback(output)
        }
        val tagsAll =
            providers.exec {
                commandLine("git", "tag", "-l", "v*")
            }.standardOutput.asText.get().trim().lines().filter { it.isNotBlank() }
        // Exclude tags pointing at the commit being built: a tagged release
        // commit legitimately has the same versionCode as its own tag.
        val headTags =
            providers.exec {
                commandLine("git", "tag", "--points-at", "HEAD")
            }.standardOutput.asText.get().trim().lines().filter { it.isNotBlank() }.toSet()
        val tags = tagsAll.filter { it !in headTags }
        if (tags.isEmpty()) {
            logger.lifecycle("checkVersionBump: no prior v* tags to compare against, skipping")
            return@doLast
        }

        fun verKey(tag: String): List<Int> = tag.removePrefix("v").split('.').map { it.toIntOrNull() ?: 0 }
        val sorted =
            tags.sortedWith { a, b ->
                val ka = verKey(a)
                val kb = verKey(b)
                val n = maxOf(ka.size, kb.size)
                for (i in 0 until n) {
                    val va = ka.getOrElse(i) { 0 }
                    val vb = kb.getOrElse(i) { 0 }
                    if (va != vb) return@sortedWith va - vb
                }
                0
            }
        val latestTag = sorted.last()
        val tagVersionCode =
            parseVersionCode(
                providers.exec {
                    commandLine("git", "show", "$latestTag:./app/build.gradle.kts")
                }.standardOutput.asText.get(),
            )
        if (tagVersionCode == null) {
            throw GradleException(
                "checkVersionBump: could not read appVersionCode from $latestTag — " +
                    "verify app/build.gradle.kts defines 'val appVersionCode'",
            )
        }
        if (appVersionCode <= tagVersionCode) {
            throw GradleException(
                "checkVersionBump: versionCode $appVersionCode must be greater than " +
                    "$tagVersionCode (from $latestTag). Bump appVersionCode and appVersionName " +
                    "in app/build.gradle.kts before releasing.",
            )
        }
        logger.lifecycle("checkVersionBump: OK — versionCode $appVersionCode > $tagVersionCode ($latestTag)")
    }
}

tasks.matching { it.name in setOf("assembleRelease", "assembleDebug") }.configureEach {
    dependsOn("checkVersionBump")
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

// Single source of truth for the app version. The self-updater
// (AppUpdater.isNewer) compares BuildConfig.VERSION_NAME against the
// latest GitHub release tag, so these MUST be bumped together on every
// release. Bump appVersionCode by 1 and appVersionName to the tag name
// (without the leading 'v').
val appVersionCode = 44
val appVersionName = "1.6.16"

android {
    namespace = "com.aplusstudyhouse.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aplusstudyhouse.app"
        minSdk = 24
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField("String", "VERSION_NAME", "\"$appVersionName\"")
        buildConfigField("int", "VERSION_CODE", "$appVersionCode")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile =
                if (keystorePropertiesFile.exists()) {
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
                "proguard-rules.pro",
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

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${supabaseProperties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${supabaseProperties.getProperty("SUPABASE_ANON_KEY", "")}\"",
        )
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

    // EXIF orientation for child profile photos taken with the camera
    implementation("androidx.exifinterface:exifinterface:1.3.7")

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
