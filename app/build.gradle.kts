import com.android.build.api.dsl.AaptOptions

plugins {
    alias(libs.plugins.android.application)
}

android {
    aaptOptions {
        noCompress("tflite")
        noCompress("task")
    }


    namespace = "vn.edu.mku.ecoscan"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "vn.edu.mku.ecoscan"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16.1")
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    implementation("com.google.mediapipe:tasks-vision:1.0.0")
    implementation("org.tensorflow:tensorflow-lite:2.17.0")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup.picasso:picasso:2.8")
}
