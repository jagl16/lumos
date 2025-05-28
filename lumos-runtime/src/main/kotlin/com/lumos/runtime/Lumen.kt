package com.lumos.runtime

/**
 * Represents a data entry for a logged function call, capturing details about the call site.
 * This data is typically collected by the Lumos compiler plugin and used for analysis or debugging.
 */
data class Lumen(
    /** The absolute path to the source file where the function call occurred. */
    val filePath: String,
    /** The name of the source file. */
    val fileName: String,
    /** The line number in the source file where the function call is located. */
    val lineNumber: Int,
    /** The fully qualified name of the function that was called. */
    val targetFunctionName: String
)
