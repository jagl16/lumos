plugins {
    // kotlin("jvm") version "1.9.22" // Applied from root
    `maven-publish`
}

// group and version are inherited from the root project

repositories {
    mavenCentral()
}

dependencies {
    // implementation(kotlin("stdlib")) // Inherited from root
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
            artifactId = project.name // "lumos-runtime"
            version = project.version.toString()

            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}
