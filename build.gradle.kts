import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    java
    id("fabric-loom") version "0.8-SNAPSHOT" apply false
    kotlin("jvm") version "1.5.10"
    id("net.minecrell.licenser") version "0.4.1"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    `maven-publish`
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

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

allprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "net.minecrell.licenser")
    apply(plugin = "com.github.johnrengelman.shadow")

    version = rootProject.version
    group = rootProject.group
    buildDir = rootProject.buildDir

    license {
        header = rootProject.file("HEADER")
        include("**/*.java", "**/*.kt")
    }

    dependencies {
        val modImplementation by configurations

        add("minecraft", "com.mojang:minecraft:$minecraftVersion") {
            isTransitive = false
        }
        add("mappings", "net.fabricmc:yarn:$yarnMappings:v2")

        modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
        modImplementation("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    }

    val shade by configurations.creating
    val modShade by configurations.creating

    configurations {
        create("dev")
        compileOnly.get().extendsFrom(shade, create("shadeSource"))
        this["modCompileOnly"].extendsFrom(modShade)
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = javaVersion.toString()
        }

        withType(JavaCompile::class).configureEach {
            options.encoding = "UTF-8"
            options.release.set(javaVersion.ordinal + 1) //epic
        }

        jar {
            enabled = false
            //archiveClassifier.set("dev")
        }

        this["remapJar"].enabled = false

        withType<ShadowJar> {
            enabled = true
            configurations = listOf(shade, modShade)
        }

        val shadowJar by this
        val remapSourcesJar by this

        val remapShadowJar by creating(RemapJarTask::class) {
            dependsOn(shadowJar)
            afterEvaluate {
                input.set(file("${project.buildDir}/libs/${base.archivesBaseName}-${rootProject.version}-dev.jar"))
                archiveFileName.set("${base.archivesBaseName}-${project.version}.jar")
                addNestedDependencies.set(true)
                remapAccessWidener.set(true)
            }
        }

        val remapMavenJar by creating(RemapJarTask::class) {
            dependsOn(shadowJar)
            input.set(file("${project.buildDir}/libs/${base.archivesBaseName}-${rootProject.version}-dev.jar"))
            archiveFileName.set("${base.archivesBaseName}-${project.version}-maven.jar")
            addNestedDependencies.set(false)
            remapAccessWidener.set(true)
        }

        val sourcesJar by creating(Jar::class) {
            dependsOn(classes)
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        build {
            dependsOn(shadowJar, remapShadowJar, sourcesJar, remapSourcesJar)
        }

        rootProject.tasks["publish"].dependsOn(shadowJar, remapMavenJar, sourcesJar, remapSourcesJar)
    }

    configure<net.fabricmc.loom.LoomGradleExtension> {
        shareCaches = true
    }

    java {
        withSourcesJar()
    }
}

project("base") {
    tasks.withType<ProcessResources> {
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to version,
                    "fabric_api" to fabricVersion,
                    "fabric_language_kotlin" to fabricLanguageKotlinVersion
                )
            )
        }
    }
}

dependencies {
    val modRuntime by configurations

    subprojects.forEach {
        shadow(project(path = it.name, configuration = "shadow"))
    }

    //modRuntime("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")
}

//TODO: Add maven publish block

setOf(
    "jar",
    "remapJar",
    "sourcesJar",
    "shadowJar",
    "remapJar",
    "remapSourcesJar",
    "remapShadowJar",
    "remapMavenJar"
).forEach {
    tasks[it].enabled = false
}