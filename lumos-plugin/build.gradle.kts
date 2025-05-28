plugins {
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    // For compiler plugins
    compileOnly(kotlin("compiler-embeddable")) // Or kotlin("compiler")
    // K2/FIR specific artifacts - placeholder, might need to be adjusted
    // compileOnly("org.jetbrains.kotlin:kotlin-fir-compiler-plugin-api:1.9.22") // Example, verify correct artifact
}

// Optional: Configure Kotlin compilation options if needed
// tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//     kotlinOptions {
//         jvmTarget = "1.8"
//     }
// }
