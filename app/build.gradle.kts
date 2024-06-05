plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.apollographql.apollo3").version("3.7.3")
    id("com.google.gms.google-services")
}

apollo {
    service("service") {
        packageName.set("com.omarinc.shopify")
    }
}


android {
    namespace = "com.omarinc.shopify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.omarinc.shopify"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding =true
        dataBinding =true
    }
}

dependencies {


    //Room
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    kapt ("androidx.room:room-compiler:2.6.1")
    //Coroutines Dependencies
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
    androidTestImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.2")

    //Apollo
    implementation("com.apollographql.apollo3:apollo-runtime:3.7.3")

    // Image loading
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    //ViewModel
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")

    //Google Maps
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("com.google.android.libraries.places:places:3.3.0")



    //animation
    implementation ("com.airbnb.android:lottie:6.4.0")
    //loading animation
    implementation ("com.github.leandroborgesferreira:loading-button-android:2.3.0")
    //dots viewPager
    implementation("com.tbuonomo:dotsindicator:5.0")


    implementation ("androidx.navigation:navigation-fragment:2.5.3")
    implementation ("androidx.navigation:navigation-ui:2.5.3")


    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")


    // Dependencies for local unit tests
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.hamcrest:hamcrest-all:1.3")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    testImplementation ("org.robolectric:robolectric:4.5.1")



    // AndroidX Test - JVM testing
    testImplementation ("androidx.test:core-ktx:1.4.0")
    //testImplementation "androidx.test.ext:junit:$androidXTestExtKotlinRunnerVersion"

    // AndroidX Test - Instrumented testing //Added Item here
    androidTestImplementation ("androidx.test:runner:1.1.3")
    androidTestImplementation ("androidx.test:rules:1.2.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

    //Timber
    implementation ("com.jakewharton.timber:timber:5.0.1")

    // hamcrest
    testImplementation ("org.hamcrest:hamcrest:2.2")
    testImplementation ("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation ("org.hamcrest:hamcrest:2.2")
    androidTestImplementation ("org.hamcrest:hamcrest-library:2.2")



    // AndroidX and Robolectric
    testImplementation ("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation ("androidx.test:core-ktx:1.4.0")
    testImplementation ("org.robolectric:robolectric:4.8")


    // InstantTaskExecutorRule
    testImplementation( "androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation ("androidx.arch.core:core-testing:2.1.0")


    testImplementation ("io.mockk:mockk:1.13.10")
    androidTestImplementation ("io.mockk:mockk-android:1.12.5")



    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //ImageViewShape
    implementation ("com.google.android.material:material:1.6.0")


    // circular image
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //OkHttp
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

}