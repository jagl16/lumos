package com.example

// Assuming com.lumos.runtime.Lumen is the data class that will be injected.
// If the package or class name is different, please adjust.
import com.lumos.runtime.Lumen // Ensure this import is correct

class KotlinExample {
    fun targetMethodInKotlin(lumen: Lumen, name: String) {
        println("KotlinExample.targetMethodInKotlin called:")
        println("  Name: " + name)
        println("  Lumen filePath: " + lumen.filePath)
        println("  Lumen fileName: " + lumen.fileName)
        println("  Lumen lineNumber: " + lumen.lineNumber)
        println("  Lumen targetFunctionName: " + lumen.targetFunctionName)
        // Add other Lumen fields if they exist and are relevant for MVP
    }
}
