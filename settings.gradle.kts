// settings.gradle.kts

pluginManagement { // Add this block
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // Also good to have
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// Keep the rest of your file as is
rootProject.name = "final2"
include(":app")

