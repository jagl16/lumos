package com.lumos.fir

import com.lumos.fqn.ParsedFqnData // Import ParsedFqnData
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

/**
 * Registers FIR (Frontend Intermediate Representation) extensions for the Lumos plugin.
 * This class is responsible for integrating custom logic into the Kotlin compiler's FIR pipeline,
 * by registering session-level components, function patchers, and expression resolvers.
 *
 * @param parsedFqnTargets A list of [ParsedFqnData] objects representing functions/methods
 *                         that the plugin should target. This list is passed from
 *                         [com.lumos.plugin.LumosCompilerPluginRegistrar].
 */
class LumosFirExtensionRegistrar(
    // Corrected from List<String> to List<ParsedFqnData> as per updates in Task 1.3.2 / Subtask 11
    private val parsedFqnTargets: List<ParsedFqnData>
) : FirExtensionRegistrar() {
    /**
     * Configures and registers FIR extensions within the provided [ExtensionRegistrarContext].
     * This method is called by the Kotlin compiler during the setup of FIR extensions.
     *
     * For the Lumos plugin, this phase is used to:
     * 1. Register the [LumosSessionComponent], making it available throughout the FIR session.
     *    The [LumosSessionComponent] holds the list of target FQNs.
     * 2. Register the [LumosFunctionPatcherExtension], which is responsible for identifying
     *    and modifying target functions.
     * 3. Register the [LumosFirExpressionResolutionExtension], which enables the transformation
     *    of function call sites.
     */
    override fun ExtensionRegistrarContext.configurePhase() {
        // Register LumosSessionComponent, making it available for the current FIR session.
        // The component is initialized with the parsedFqnTargets provided to this registrar.
        register(LumosSessionComponent::class, LumosSessionComponent(parsedFqnTargets))

        // Register the LumosFunctionPatcherExtension.
        // The `register` function, when given a constructor reference (::LumosFunctionPatcherExtension),
        // will correctly instantiate it, injecting the FirSession if required by its constructor.
        register(::LumosFunctionPatcherExtension)

        // Register the LumosFirExpressionResolutionExtension.
        // Similar to the patcher, this registers the extension that will handle call site transformations.
        register(::LumosFirExpressionResolutionExtension)
    }
}
