package com.lumos.gradle

import com.lumos.plugin.LumosCliOptions
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Gradle subplugin for Lumos.
 * This class integrates the Lumos compiler plugin with the Kotlin Gradle plugin.
 * It is responsible for:
 * 1. Registering the `LumosGradleExtension` to allow users to configure the plugin
 *    via their `build.gradle.kts` files.
 * 2. Passing the configured settings (e.g., target FQNs) to the Kotlin compiler
 *    as command-line options.
 *
 * This class is discovered via service loading (see META-INF/services).
 */
class LumosGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(project: Project) {
        // Register the LumosGradleExtension with the project, making it available
        // for configuration in build scripts (e.g., lumos { ... }).
        project.extensions.create(LumosCliOptions.PLUGIN_DISPLAY_NAME.lowercase(), LumosGradleExtension::class.java)
    }

    /**
     * Checks if the Lumos plugin is applicable to the given Kotlin compilation.
     * For now, it's considered applicable to all Kotlin compilations.
     *
     * @param kotlinCompilation The Kotlin compilation context.
     * @return True if the plugin should be applied, false otherwise.
     */
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    /**
     * Provides the unique identifier for the Lumos compiler plugin.
     * This must match the ID used by the `CompilerPluginRegistrar`.
     *
     * @return The compiler plugin ID.
     */
    override fun getCompilerPluginId(): String = LumosCliOptions.PLUGIN_ID

    /**
     * Specifies the Maven artifact coordinates for the Lumos compiler plugin.
     * This tells the Kotlin Gradle plugin where to find the actual plugin JAR.
     * It assumes the plugin is published with the group and version of the current project.
     *
     * @return A [SubpluginArtifact] pointing to the Lumos plugin.
     */
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.lumos", // Assuming this is the group ID, adjust if different
        artifactId = "lumos-plugin", // Name of the plugin module
        version = "0.1.0-SNAPSHOT" // Placeholder version, adjust or read from project.version
        // Consider using project.group and project.version for dynamic values if this plugin
        // is part of the same Gradle build as the project it's applied to,
        // or use fixed published coordinates.
    )

    /**
     * Applies the plugin's configuration to the given Kotlin compilation.
     * This method reads settings from the [LumosGradleExtension] and translates them
     * into [SubpluginOption] instances that are passed to the Kotlin compiler.
     *
     * @param kotlinCompilation The Kotlin compilation to configure.
     * @return A [Provider] of a list of [SubpluginOption]s for the compiler.
     */
    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val lumosExtension = project.extensions.findByType(LumosGradleExtension::class.java)
            ?: LumosGradleExtension() // Provide a default if not configured

        return project.provider {
            val options = mutableListOf<SubpluginOption>()

            // Get the FQNs from the extension. If null or empty, this will be an empty list.
            val targetFqns = lumosExtension.targetMethodSignatures.getOrElse(emptyList())

            for (fqn in targetFqns) {
                options.add(SubpluginOption(key = LumosCliOptions.FQN_OPTION.optionName, value = fqn))
            }
            options
        }
    }
}
