package com.lumos.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

/**
 * Gradle extension for configuring the Lumos compiler plugin.
 * This extension allows users to specify settings for the plugin through the Gradle build script.
 */
abstract class LumosGradleExtension {
    /**
     * A list of fully qualified method signatures that the Lumos plugin should target.
     * These signatures are used by the compiler plugin to identify specific method calls
     * for logging or other analysis.
     * Example format: "com.example.MyClass.myMethod(java.lang.String,int)"
     */
    @get:Input
    abstract val targetMethodSignatures: ListProperty<String>
}
