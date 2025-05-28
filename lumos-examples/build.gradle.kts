plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    // Dependencies on lumos-plugin and lumos-runtime will be added later
}

application {
    mainClass.set("com.lumos.examples.MainKt") // Example main class
}
