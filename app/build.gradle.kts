import com.google.protobuf.gradle.*

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.nxlinkstar.stargrader"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.nxlinkstar.stargrader"
        minSdk = 26
        targetSdk = 33
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.datastore.preferences)
//    implementation(libs.androidx.datastore)



    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.okhttp)
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") // 协程(版本自定)
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
//    implementation 'com.squareup.okhttp3:okhttp:4.10.0' // 要求OkHttp4以上
//    implementation("com.github.liangjingkanji:Net:3.5.8")

//    implementation(libs.protobuf.java)
//    implementation("io.grpc:grpc-stub:1.45.1")
//    implementation("io.grpc:grpc-protobuf:1.45.1")

    implementation(libs.gson)

}



//protobuf {
//    protoc {
//        artifact = "com.google.protobuf:protoc:3.19.3"
//    }
//
//    generateProtoTasks {
//        all().each { task ->
//            task.builtins {
//                java {
//                    option 'lite'
//                }
//            }
//        }
//    }
//}
//
protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:3.19.3"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
        }
    }


    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc"){}
            }
        }
//        ofSourceSet("main").forEach {
//            it.plugins {
//                // Apply the "grpc" plugin whose spec is defined above, without
//                // options. Note the braces cannot be omitted, otherwise the
//                // plugin will not be added. This is because of the implicit way
//                // NamedDomainObjectContainer binds the methods.
//                id("grpc") { }
//            }
//        }
    }
}