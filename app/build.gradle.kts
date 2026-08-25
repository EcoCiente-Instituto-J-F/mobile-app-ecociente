import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val propriedadesEnv = Properties()

val arquivoEnv = rootProject.file(".env")

if (arquivoEnv.exists()) {
    arquivoEnv.inputStream().use { entrada ->
        propriedadesEnv.load(entrada)
    }
}

fun obterVariavelAmbiente(nome: String): String {
    return System.getenv(nome)
        ?: propriedadesEnv.getProperty(nome)
        ?: error(
            "Variável de ambiente não encontrada: $nome. " +
                    "Adicione-a no arquivo .env da raiz do projeto."
        )
}

android {
    namespace = "com.example.ecociente"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ecociente"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "facebook_app_id", obterVariavelAmbiente("FACEBOOK_APP_ID"))

        resValue("string", "fb_login_protocol_scheme", obterVariavelAmbiente("FACEBOOK_LOGIN_PROTOCOL_SCHEME"))

        resValue("string", "facebook_client_token", obterVariavelAmbiente("FACEBOOK_CLIENT_TOKEN"))
    }

    buildFeatures {
        resValues = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11

        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.16.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.facebook.android:facebook-login:18.2.3")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}