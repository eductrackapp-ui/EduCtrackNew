pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // ✅ Pour les librairies hébergées sur GitHub (ex: MPAndroidChart, EmailJS)
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ✅ Pour les dépendances GitHub via JitPack
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "EduCtrack"
include(":app")
