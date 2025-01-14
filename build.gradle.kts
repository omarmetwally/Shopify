// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript{
    dependencies{
        classpath( "androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }
}


plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false

}