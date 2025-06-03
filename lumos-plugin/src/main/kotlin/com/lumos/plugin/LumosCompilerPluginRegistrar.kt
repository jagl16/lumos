package com.lumos.plugin

import com.lumos.fir.LumosFirExtensionRegistrar
import com.lumos.fqn.FqnParser // Added import
import com.lumos.fqn.ParsedFqnData // Added import
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class LumosCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        // Retrieve the FQN strings passed as command line options
        val fqnStrings = configuration.get(LumosCliOptions.FQN_OPTION) ?: emptyList()

        val fqnParser = FqnParser() // Instantiate FqnParser
        val parsedFqns = fqnStrings.mapNotNull { fqnString ->
            try {
                fqnParser.parse(fqnString)
            } catch (e: IllegalArgumentException) {
                // Optionally, report a warning/error to the user via MessageCollector
                // For now, just return null to filter it out
                // configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)?.report(...)
                null
            }
        }

        // Pass the parsed FQNs to the FirExtensionRegistrar
        FirExtensionRegistrar.registerExtension(LumosFirExtensionRegistrar(parsedFqns))
    }

    override val supportsK2: Boolean
        get() = true
}
