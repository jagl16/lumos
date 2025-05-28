package com.lumos.fqn

/**
 * A utility object for parsing fully qualified names (FQNs) of methods.
 * It provides a method to break down an FQN string into its constituent parts:
 * class FQN, method name, and parameter types.
 */
object FqnParser {
    /**
     * Parses a given method FQN string into a [ParsedFqnData] object.
     *
     * The expected FQN format is `com.example.ClassName.methodName(paramType1,paramType2,...)`
     * or `com.example.ClassName.methodName` if the method has no parameters.
     *
     * The parsing logic handles:
     * - Splitting the class FQN and method name.
     * - Extracting parameter types from the parenthesized list. Parameter types are expected
     *   to be comma-separated.
     * - Handling FQNs with or without parameters.
     * - Basic validation for the structure (e.g., presence of class and method parts,
     *   correct parentheses for parameters).
     *
     * @param fqn The fully qualified name string of the method to parse.
     * @return A [ParsedFqnData] object containing the parsed components if the FQN string is
     *         valid and successfully parsed. Returns `null` if the FQN string is malformed,
     *         cannot be parsed, or if any unexpected error occurs during parsing.
     */
    fun parse(fqn: String): ParsedFqnData? {
        try {
            val openParenIndex = fqn.indexOf('(')
            val closeParenIndex = if (openParenIndex != -1) fqn.lastIndexOf(')') else -1

            val classAndMethodPart: String
            val parameterTypes: List<String>

            if (openParenIndex != -1 && closeParenIndex != -1 && openParenIndex < closeParenIndex) {
                // Case with parameters
                classAndMethodPart = fqn.substring(0, openParenIndex)
                val paramsString = fqn.substring(openParenIndex + 1, closeParenIndex)
                parameterTypes = if (paramsString.isBlank()) {
                    emptyList()
                } else {
                    paramsString.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                }
            } else if (openParenIndex == -1 && closeParenIndex == -1) {
                // Case without parameters
                classAndMethodPart = fqn
                parameterTypes = emptyList()
            } else {
                // Malformed (e.g., mismatched parentheses)
                return null
            }

            val lastDotIndex = classAndMethodPart.lastIndexOf('.')
            if (lastDotIndex == -1 || lastDotIndex == 0 || lastDotIndex == classAndMethodPart.length - 1) {
                // Malformed (e.g., ".method" or "Class." or "NoDotsHere")
                return null
            }

            val classFqn = classAndMethodPart.substring(0, lastDotIndex)
            val methodName = classAndMethodPart.substring(lastDotIndex + 1)

            if (methodName.isEmpty() || classFqn.isEmpty()) {
                return null
            }
            
            // Basic validation for method name (e.g., should not contain '.')
            if (methodName.contains('.')) {
                return null
            }

            return ParsedFqnData(
                classFqn = classFqn,
                methodName = methodName,
                parameterTypes = parameterTypes,
                originalFqn = fqn
            )
        } catch (e: Exception) {
            // Catch any unexpected errors during parsing
            return null
        }
    }
}
