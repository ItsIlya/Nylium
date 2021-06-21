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

sourceSets {
    main {
        java.srcDir("${rootProject.rootDir}/api/src/main/java")
    }
//    getByName("kotlin") {
//        java.srcDir("${rootProject.rootDir}/api/src/main/kotlin")
//    }

}