import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.gradle)

}

android {
    namespace = "com.example.plana"
    compileSdk = 35

    packagingOptions {
        pickFirsts += "META-INF/INDEX.LIST"
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
    }

    defaultConfig {
        applicationId = "com.mvp.plana"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
//    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization)
    implementation(libs.androidx.navigation)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    androidTestImplementation(libs.hilt.android.testing)
    testImplementation(libs.hilt.android.testing)
    implementation(libs.retrofit)
    implementation(libs.serialization.converter)
    implementation(libs.coil)
    kapt(libs.hilt.compiler)
//    implementation(libs.google.api.client)
//    {
//        exclude("org.apache.httpcomponents")
//    }
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.google.api.services.calendar)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.playservices)
    implementation(libs.google.libraries.identity)
    implementation(libs.play.services)
    implementation(libs.g.api.client) {
        exclude("org.apache.httpcomponents")
    }
    implementation(libs.google.accompanist)
    implementation(libs.google.http)

    implementation(libs.composecalendar)
    implementation(libs.kotlinx.datetime)

}