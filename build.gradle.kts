// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}

buildscript {

    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/public/")
        }
        maven{
            url = uri("https://jitpack.io")
        }
        mavenLocal()
        mavenCentral()
        google()
    }
    dependencies {
//        classpath("com.android.tools.build:gradle:8.1")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:231-1.8.21-release-380-AS9011.34.2311.10366083")
    }
}

true // Needed to make the Suppress annotation work for the plugins block
