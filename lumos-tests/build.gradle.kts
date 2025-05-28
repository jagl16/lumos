plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.jgarcia.lumos" // Consistent with root project (assuming root is com.jgarcia.lumos)
version = "0.1.0" // Consistent with root project (assuming root version is 0.1.0)


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test")) // For writing tests (e.g., JUnit or KotlinTest)

    // Dependencies on kotlin-compile-testing, lumos-plugin, and lumos-runtime
    // will be added later after local publishing setup or when specific versions are known.
    // For example:
    // testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0") // Check for latest/K2 compatible
    // testImplementation(project(":lumos-plugin")) // If it were a subproject
    // testImplementation(project(":lumos-runtime")) // If it were a subproject
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform() // Or useJUnit() if using JUnit 4
}
