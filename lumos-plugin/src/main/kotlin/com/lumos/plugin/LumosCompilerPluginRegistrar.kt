package com.lumos.plugin

import com.lumos.fir.LumosFirExtensionRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * The main entry point for the Lumos compiler plugin.
 * This class is responsible for registering the necessary components with the Kotlin compiler,
 * particularly for the K2 compiler pipeline (FIR).
 *
 * It is registered as a service in
 * `META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar`.
 */
@OptIn(ExperimentalCompilerApi::class)
class LumosCompilerPluginRegistrar : CompilerPluginRegistrar() {

    /**
     * Registers compiler extensions into the provided [ExtensionStorage].
     * This method is called by the Kotlin compiler to allow plugins to integrate
     * their custom logic.
     *
     * For the Lumos plugin, this method initializes and registers the [LumosFirExtensionRegistrar].
     * Currently, it passes an empty list of target FQNs to the [LumosFirExtensionRegistrar].
     * In a future implementation, this list will be populated from the settings provided
     * by the [com.lumos.gradle.LumosGradleExtension].
     *
     * @param configuration The current compiler configuration, which might be used to
     *                      retrieve plugin-specific settings in more advanced scenarios.
     */
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        // Pass an empty list of FQNs for now.
        // This list will eventually be populated from the Gradle extension.
        FirExtensionRegistrar.registerExtension(LumosFirExtensionRegistrar(emptyList()))
    }

    /**
     * Indicates whether this compiler plugin registrar supports the K2 compiler frontend (FIR).
     * For Lumos, this is set to `true` as it targets the K2 pipeline.
     */
    override val supportsK2: Boolean
        get() = true
}
