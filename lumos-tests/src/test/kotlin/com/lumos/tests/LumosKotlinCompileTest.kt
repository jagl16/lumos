package com.lumos.tests

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class LumosKotlinCompileTest {

    // Companion object to hold utility methods or shared state for tests
    companion object {
        // This list could be used to capture Lumen data from within compiled test code
        // by having the test code add strings to it.
        val capturedLumenData = mutableListOf<String>()

        // Method that can be called from compiled test code to record Lumen details.
        // Ensure this method is public and static (or in a companion object) to be callable.
        @JvmStatic // Important for Java interop if called from Java compiled code, also good for Kotlin.
        fun recordLumenDetails(
            filePath: String,
            fileName: String,
            lineNumber: Int,
            targetFunctionName: String,
            // Potentially add more Lumen fields here as they become available/testable
            // e.g., callSiteHash: String, className: String?, callingFunctionName: String?
        ) {
            val detailString = "Lumen: filePath='$filePath', fileName='$fileName', lineNumber=$lineNumber, targetFunctionName='$targetFunctionName'"
            capturedLumenData.add(detailString)
            println("Captured from test code: $detailString") // For live logging during tests
        }
    }

    // Test for FQN-based targeting of Kotlin functions
    @Test
    fun `kotlin FQN targeted function should receive Lumen object`() {
        // Test logic for FQN will be added here (Step 6.b)
        assertTrue(true, "Placeholder for FQN test")
    }

    // Test for Annotation-based targeting of Kotlin functions
    @Test
    fun `kotlin @LumosMaxima annotated function should receive Lumen object`() {
        // Test logic for annotation will be added here (Step 6.c)
        assertTrue(true, "Placeholder for Annotation test")
    }

    // Helper function to configure and run a Kotlin compilation
    private fun compileAndRun(
        sourceFiles: List<SourceFile>,
        fqnTargets: List<String> = emptyList(),
        useAnnotations: Boolean = false, // Flag to indicate if annotation processing should be implicitly active
        mainClass: String,
        mainMethod: String = "main"
    ): KotlinCompilation.Result {
        val compilation = KotlinCompilation().apply {
            sources = sourceFiles
            compilerPlugins = listOf(com.lumos.plugin.LumosCompilerPluginRegistrar()) // Register our plugin
            // For FQN targeting, pass options to the plugin
            if (fqnTargets.isNotEmpty()) {
                pluginOptions = fqnTargets.map { "plugin:com.lumos.plugin:fqn=$it" }
            }
            // For annotation targeting, the plugin should pick it up automatically if configured to do so.
            // No specific option might be needed here if LumosFunctionPatcherExtension handles annotations.

            inheritClassPath = true // Important for accessing runtime libraries like Lumen itself
            messageOutputStream = System.out // To see compiler messages
            verbose = true // For more detailed output
        }

        val result = compilation.compile()

        if (result.exitCode == KotlinCompilation.ExitCode.OK) {
            try {
                val kClazz = result.classLoader.loadClass(mainClass)
                val mainMethod = kClazz.getMethod(mainMethod) // Assuming a static main method without args
                mainMethod.invoke(null) // Invoke static main
            } catch (e: Exception) {
                throw RuntimeException("Failed to execute compiled code", e)
            }
        }
        return result
    }
}
