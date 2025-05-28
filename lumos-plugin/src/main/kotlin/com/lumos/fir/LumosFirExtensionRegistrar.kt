package com.lumos.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
// Removed unused imports like FirExtensionRegistrarAdapter, sessionContainer, etc. for clarity

/**
 * Registers FIR (Frontend Intermediate Representation) extensions for the Lumos plugin.
 * This class is responsible for integrating custom logic into the Kotlin compiler's FIR pipeline,
 * specifically by registering session-level components.
 *
 * @param fqnTargets A list of fully qualified names of functions/methods that the plugin
 *                   should target for analysis or transformation. This list is currently
 *                   passed from [com.lumos.plugin.LumosCompilerPluginRegistrar].
 */
class LumosFirExtensionRegistrar(
    private val fqnTargets: List<String>
) : FirExtensionRegistrar() {
    /**
     * Configures and registers FIR extensions within the provided [ExtensionRegistrarContext].
     * This method is called by the Kotlin compiler during the setup of FIR extensions.
     *
     * For the Lumos plugin, this phase is used to register the [LumosSessionComponent],
     * making it available throughout the FIR session. The [LumosSessionComponent] holds
     * the list of target FQNs that other FIR processors might use.
     */
    override fun ExtensionRegistrarContext.configurePhase() {
        // Register LumosSessionComponent, making it available for the current FIR session.
        // The component is initialized with the fqnTargets provided to this registrar.
        register(LumosSessionComponent::class, LumosSessionComponent(fqnTargets))
    }
}
