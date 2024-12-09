// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    // alias(libs.plugins.gms) apply false //from bootcamp tutorial, doesn't compile
    id("com.google.gms.google-services") version "4.4.2" apply false // from firebase creation guide
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false // for room
}