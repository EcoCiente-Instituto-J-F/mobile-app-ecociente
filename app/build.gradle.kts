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

        // URL pública configurável, sem exigir um .env novo nos builds existentes.
        val mobileApiBaseUrl = (System.getenv("ECOCIENTE_MOBILE_API_BASE_URL")
            ?: propriedadesEnv.getProperty("ECOCIENTE_MOBILE_API_BASE_URL")
            ?: "https://ds-mobile-api.onrender.com").trim().trimEnd('/')

        require(mobileApiBaseUrl.startsWith("https://")) {
            "ECOCIENTE_MOBILE_API_BASE_URL deve usar HTTPS"
        }

        resValue("string", "educacao_api_base_url", mobileApiBaseUrl)
        buildConfigField("String", "ECOCIENTE_MOBILE_API_BASE_URL", "\"$mobileApiBaseUrl\"")

        val ekoChatUrl = (System.getenv("ECOCIENTE_EKO_CHAT_URL")
            ?: propriedadesEnv.getProperty("ECOCIENTE_EKO_CHAT_URL")
            ?: "https://api.52.71.117.143.sslip.io/api/v1/chat").trim()

        require(ekoChatUrl.startsWith("https://")) {
            "ECOCIENTE_EKO_CHAT_URL deve usar HTTPS"
        }

        resValue("string", "eko_chat_url", ekoChatUrl)

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Só o build type `demo` liga o modo demonstração; nos demais a tela de login não muda.
        buildConfigField("boolean", "MODO_DEMO", "false")
        buildConfigField("String", "DEMO_EMAIL", "\"\"")
        buildConfigField("String", "DEMO_SENHA", "\"\"")

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
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // APK de apresentação: API falsa dentro do processo, sem rede nem Firebase (código em src/demo).
        // Gera-se com ./gradlew assembleDemo. As credenciais abaixo têm de ser iguais a SementeDemo.EMAIL e SENHA.
        create("demo") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")

            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"

            buildConfigField("boolean", "MODO_DEMO", "true")
            buildConfigField("String", "DEMO_EMAIL", "\"morador@ecociente.com.br\"")
            buildConfigField("String", "DEMO_SENHA", "\"Ecociente2026.\"")

            // Hosts fixos: um env var ou .env de quem compila não pode levar a demo a um host que a FabricaConexoesDemo recusa.
            buildConfigField("String", "ECOCIENTE_MOBILE_API_BASE_URL", "\"https://ds-mobile-api.onrender.com\"")
            resValue("string", "educacao_api_base_url", "https://ds-mobile-api.onrender.com")
            resValue("string", "eko_chat_url", "https://api.52.71.117.143.sslip.io/api/v1/chat")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Cliente HTTP com suporte nativo ao método PATCH usado pela API de agendamentos.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

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