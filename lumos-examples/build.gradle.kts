plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":lumos-runtime"))
    kotlinCompilerPluginClasspath(project(":lumos-plugin"))
    // Dependencies on lumos-plugin and lumos-runtime will be added later
}

application {
    mainClass.set("com.lumos.examples.MainKt") // Example main class
}

lumos {
    targetMethodSignatures.set(listOf(
        "com.example.KotlinExample.targetMethodInKotlin(java.lang.String)",
        "com.example.JavaExample.targetMethodInJava(int)"
    ))
}
