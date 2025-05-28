package com.lumos.runtime

/**
 * Annotate a function with @LumosMaxima to automatically inject a Lumen object
 * as the last parameter. This signals the Lumos compiler plugin to modify
 * the function's signature and update all call sites.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE) // Or BINARY if needed for other tools, SOURCE is typical for compiler plugins
annotation class LumosMaxima
