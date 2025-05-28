plugins {
    kotlin("jvm") version "1.9.22" apply false // Apply to subprojects
    `maven-publish` apply false // Apply to subprojects that need it
}

group = "com.jgarcia.lumos"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure subprojects
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))
    }

    // Apply group and version from root project
    group = rootProject.group
    version = rootProject.version
}
