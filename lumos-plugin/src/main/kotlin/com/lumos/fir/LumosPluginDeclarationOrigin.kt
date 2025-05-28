package com.lumos.fir

import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin

/**
 * Custom FIR declaration origin for elements generated or modified by the Lumos plugin.
 * This helps in identifying plugin-specific contributions to the FIR tree,
 * which can be useful for debugging or for other compiler phases.
 */
object LumosPluginDeclarationOrigin : FirDeclarationOrigin.Plugin() {
    override val pluginName: String = "Lumos"
}
