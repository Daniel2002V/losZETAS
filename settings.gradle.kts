pluginManagement {
    repositories {
        google() // Dejamos la declaración simple
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // ← AGREGAR ESTA LÍNEA

    }
}

rootProject.name = "losZETAS"
include(":app")
