import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

val propriedadesEnv = Properties()

val arquivoEnv = rootProject.file(".env")

if (arquivoEnv.exists()) {
    arquivoEnv.inputStream().use { entrada ->
        propriedadesEnv.load(entrada)
    }
}

val propriedadesLocais = Properties()

val arquivoLocal = rootProject.file("local.properties")

if (arquivoLocal.exists()) {
    arquivoLocal.inputStream().use { entrada ->
        propriedadesLocais.load(entrada)
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

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Chave da Routes API (distância até as cooperativas). Opcional: sem ela o app usa o Geocoder.
        resValue(
            "string",
            "maps_api_key",
            propriedadesLocais.getProperty("MAPS_API_KEY") ?: ""
        )

        resValue(
            "string",
            "facebook_app_id",
            obterVariavelAmbiente("FACEBOOK_APP_ID")
        )

        resValue(
            "string",
            "fb_login_protocol_scheme",
            obterVariavelAmbiente("FACEBOOK_LOGIN_PROTOCOL_SCHEME")
        )

        resValue(
            "string",
            "facebook_client_token",
            obterVariavelAmbiente("FACEBOOK_CLIENT_TOKEN")
        )
    }

    buildFeatures {
        resValues = true
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
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Necessário para as notificações push do EcoCiente.
    // Continua usando a versão gerenciada pelo Firebase BoM.
    implementation("com.google.firebase:firebase-messaging")

    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.facebook.login)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.interpolator)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    implementation(libs.swiperefreshlayout)

    implementation(libs.glide)

    testImplementation(libs.junit)
    // O org.json do android.jar é só um esqueleto: nos testes de JVM entra a implementação real.
    testImplementation("org.json:json:20240303")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}