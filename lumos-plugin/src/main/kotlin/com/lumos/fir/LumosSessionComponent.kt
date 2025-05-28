package com.lumos.fir

import org.jetbrains.kotlin.fir.FirSessionComponent

/**
 * A FIR session component for the Lumos plugin.
 * This component holds data or services that are relevant for the duration of a FIR session.
 * In this case, it stores the list of fully qualified names (FQNs) of target methods
 * that the plugin is interested in.
 *
 * This component is registered by [LumosFirExtensionRegistrar] and can be accessed by
 * other FIR extensions or processors during the compilation phases.
 *
 * @property fqnTargets A list of strings, where each string is a fully qualified name
 *                      of a method or function that the Lumos plugin should analyze or process.
 */
class LumosSessionComponent(
    val fqnTargets: List<String>
) : FirSessionComponent
