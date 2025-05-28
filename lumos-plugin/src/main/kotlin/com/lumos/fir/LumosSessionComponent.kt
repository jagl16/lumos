package com.lumos.fir

import org.jetbrains.kotlin.fir.FirSessionComponent
import com.lumos.fqn.ParsedFqnData
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol // Added import for FirFunctionSymbol

/**
 * A FIR session component for the Lumos plugin.
 * This component holds data or services that are relevant for the duration of a FIR session.
 * It stores the list of parsed fully qualified names (FQNs) of target methods
 * and the symbols of functions that have been transformed by the plugin.
 *
 * This component is registered by [LumosFirExtensionRegistrar] and can be accessed by
 * other FIR extensions or processors during the compilation phases.
 *
 * @property parsedFqnTargets A list of [ParsedFqnData] objects, where each object
 *                            represents a method or function that the Lumos plugin
 *                            should analyze or process.
 * @property transformedFunctionSymbols A mutable set of [FirFunctionSymbol]s representing functions
 *                                      whose signatures have been modified by the Lumos plugin
 *                                      (e.g., by adding a Lumen parameter). This set is used
 *                                      by call site transformers to identify calls to these
 *                                      modified functions.
 */
class LumosSessionComponent(
    val parsedFqnTargets: List<ParsedFqnData>
) : FirSessionComponent {
    val transformedFunctionSymbols: MutableSet<FirFunctionSymbol<*>> = mutableSetOf()
}
