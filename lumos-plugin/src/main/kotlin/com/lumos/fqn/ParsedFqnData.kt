package com.lumos.fqn

/**
 * Represents the deconstructed components of a fully qualified method name (FQN).
 * This data class holds the parsed information from an FQN string, making it easier
 * to access individual parts like class name, method name, and parameter types.
 *
 * Instances of this class are typically created by [FqnParser].
 *
 * @property classFqn The fully qualified name of the class containing the method.
 *                    Example: "com.example.MyClass"
 * @property methodName The name of the method.
 *                      Example: "myMethod"
 * @property parameterTypes A list of strings, where each string is the fully qualified
 *                          type of a parameter. For methods with no parameters, this list is empty.
 *                          Example: `listOf("java.lang.String", "int")`
 * @property originalFqn The original, unparsed FQN string from which this data was derived.
 *                       Example: "com.example.MyClass.myMethod(java.lang.String,int)"
 */
data class ParsedFqnData(
    val classFqn: String,
    val methodName: String,
    val parameterTypes: List<String>,
    val originalFqn: String
)
