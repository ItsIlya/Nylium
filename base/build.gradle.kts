plugins {
    java
    kotlin("jvm")
}

repositories {
}

dependencies {
    compileOnly(project(":api"))
}

loom {
}