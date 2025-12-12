import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

// 獲取 Git Commit Hash
fun getGitCommitHash(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

// 獲取當前編譯日期
fun getBuildDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

android {
    namespace = "com.soundinteractionapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.soundinteractionapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ 生成 BuildConfig 欄位
        buildConfigField("String", "COMMIT_HASH", "\"${getGitCommitHash()}\"")
        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true  // ✅ 啟用 BuildConfig
    }

    ndkVersion = "27.0.12077973"
}

dependencies {

    // AndroidX 基礎
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Lifecycle Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // Coil (支援 GIF 動畫)
    implementation(libs.coil.compose)
    implementation("io.coil-kt:coil-gif:2.5.0")
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}