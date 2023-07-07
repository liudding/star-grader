pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/public/")
        }
        maven{
            url = uri("https://jitpack.io")
        }
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/public/")
        }
        maven{
            url = uri("https://jitpack.io")
        }
        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "StarGrader"
include(":app")
