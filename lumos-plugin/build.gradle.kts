plugins {
    `maven-publish`
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
//         jvmTarget = "1.8" // Inherited from root or defaults
//     }
// }

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name // "lumos-plugin"
            version = project.version.toString()

            from(components["java"]) // "java" component includes classes and resources
        }
    }
    repositories {
        mavenLocal()
    }
}
