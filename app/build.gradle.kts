plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.firebase.database)
    implementation(libs.picasso)
    implementation(libs.mpAndroidChart)
    implementation(libs.wordCloud) {
        // exclude due to duplicate classes with the
        // edu.stanford.nlp:stanford-corenlp dependency for data processing
       //exclude(group="com.sun.xml.bind", module="jaxb-core")
       // exclude(group="com.sun.xml.bind", module="jaxb-impl")
        exclude(group="com.sun.xml.bind", module="jaxb-core")
        exclude(group="com.sun.xml.bind", module="jaxb-impl")


    }
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.Gruzer:simple-gauge-android:0.3.1")
}


// Appliquer le plugin de services Google
apply(plugin = "com.google.gms.google-services")



