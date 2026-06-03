import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// ← Leitura do local.properties AQUI, fora do android { }
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

// Flag que decide se builds debug usam backend de produção (Render) ou local.
// Default: produção. Para testar contra backend local, adicione no local.properties:
//   USE_PRODUCTION=false
// (Esse arquivo não vai para o git, então sua escolha não afeta ninguém.)
val useProductionDebug: Boolean =
    localProps.getProperty("USE_PRODUCTION", "true").toBoolean()

android {
    namespace = "io.poupai.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.poupai.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "BRAPI_TOKEN", "\"${localProps.getProperty("BRAPI_TOKEN", "")}\"")
        buildConfigField("String", "PROD_BACKEND_URL", "\"https://poupai-backend.onrender.com/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            // Builds de release apontam para o backend de produção (Render).
            buildConfigField("boolean", "USE_PRODUCTION", "true")
        }
        debug {
            isDebuggable = true
            // Valor lido do local.properties (USE_PRODUCTION). Default = true.
            // Para usar backend local, defina USE_PRODUCTION=false no seu local.properties.
            buildConfigField("boolean", "USE_PRODUCTION", "$useProductionDebug")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Biometric / App Lock
    implementation(libs.androidx.biometric)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}