pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
    }
}

include("api", "base")
