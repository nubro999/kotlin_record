plugins {
    alias(libs.plugins.android.library)  // com.android.library 플러그인
    alias(libs.plugins.kotlin.android)   // kotlin-android 플러그인
    alias(libs.plugins.ksp)              // KSP 플러그인
}

dependencies {
    // 프로젝트 의존성
    implementation(project(":stt:domain"))

    // AndroidX 기본 라이브러리
    implementation(libs.androidx.core.ktx)

    // Koin 설정 (AI 모듈과 같은 방식 적용)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.androidx.runtime.android)
    ksp(libs.koin.ksp.compiler)

    // 코루틴
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // 테스트 의존성
    testImplementation(libs.junit)
}

android {
    namespace = "com.mhss.app.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}