package com.example

fun main() {
    println("Starting Lumos example application...")

    val kotlinExample = KotlinExample()
    println("\nCalling Kotlin target method...")
    kotlinExample.targetMethodInKotlin("Test Kotlin Call") // Plugin should inject Lumen

    println("\nCalling Java target method...")
    val javaExample = JavaExample()
    javaExample.targetMethodInJava(123) // Plugin should inject Lumen

    println("\nLumos example application finished.")
}
