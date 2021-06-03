plugins {
    kotlin("jvm") version "1.5.10"
    id("fabric-loom") version "0.8-SNAPSHOT" apply false
    id("net.minecrell.licenser") version "0.4.1"
    //`maven-publish`
}

fun prop(key: String) = properties[key] as? String ?: throw IllegalArgumentException("Invalid key")

base.archivesBaseName = "nylium"
group = "io.github.nyliumpowered.nylium"
version = prop("version")

val javaVersion = JavaVersion.VERSION_16
val minecraftVersion = prop("minecraft_version")
val yarnMappings = prop("yarn_mappings")
val loaderVersion = prop("loader_version")
val fabricVersion = prop("fabric_version")
val fabricLanguageKotlinVersion = prop("fabric_language_kotlin")

allprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "net.minecrell.licenser")

    version = rootProject.version
    group = rootProject.group
    buildDir = rootProject.buildDir

    license {
        header = rootProject.file("HEADER")
        include("**/*.java", "**/*.kt")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        val modImplementation by configurations

        add("minecraft", "com.mojang:minecraft:$minecraftVersion") {
            isTransitive = false
        }
        add("mappings", "net.fabricmc:yarn:$yarnMappings:v2")

        modImplementation(group = "net.fabricmc", name = "fabric-loader", version = loaderVersion)
        modImplementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = fabricLanguageKotlinVersion)
        modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = fabricVersion)
    }
}