package com.lumos.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption

/**
 * Defines the command-line interface options for the Lumos compiler plugin.
 * These options are used to pass configuration from Gradle (or other build systems)
 * to the compiler plugin during the compilation process.
 */
object LumosCliOptions {

    /**
     * Command-line option to specify a fully qualified name (FQN) of a method
     * to be targeted by the Lumos plugin.
     * This option can be provided multiple times for multiple targets.
     *
     * Syntax: -P plugin:com.lumos.plugin:fqn=<the.method.Fqn>
     */
    val FQN_OPTION: AbstractCliOption = object : AbstractCliOption(
        optionName = "fqn",
        valueDescription = "<fully.qualified.name>",
        description = "Fully qualified name of a method to target",
        required = false,
        allowMultipleOccurrences = true
    ) {}

    // Array of all options provided by the Lumos plugin.
    // This is typically used by the CompilerPluginRegistrar.
    val ALL_OPTIONS: Array<AbstractCliOption> = arrayOf(FQN_OPTION)

    // Constants for plugin ID and name, useful for the Gradle subplugin.
    const val PLUGIN_ID: String = "com.lumos.plugin"
    const val PLUGIN_DISPLAY_NAME: String = "Lumos"
}
