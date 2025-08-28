plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.smartzone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smartzone"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.google.protobuf" && requested.name == "protobuf-lite") {

                useTarget("com.google.protobuf:protobuf-javalite:3.25.3")
            }
        }

        resolutionStrategy.force("com.google.protobuf:protobuf-javalite:3.25.3")
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
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.espresso.contrib)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.intents)
    implementation(libs.material)
    androidTestImplementation(platform(libs.firebase.bom))
    androidTestImplementation(libs.androidx.espresso.core) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.espresso.intents) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.espresso.contrib) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.junit) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.test.rules) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.test.runner) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.ui.test.junit4) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation(libs.protobuf.javalite)
    androidTestImplementation(libs.protobuf.javalite)


}