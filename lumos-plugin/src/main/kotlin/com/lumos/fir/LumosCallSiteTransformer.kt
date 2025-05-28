package com.lumos.fir

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirRealSourceElementKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingFileSymbol
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.isPrimary 
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.ConstantValueKind 
import org.jetbrains.kotlin.fir.expressions.FirFunctionCallOrigin 
import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList 
import org.jetbrains.kotlin.fir.expressions.builder.buildConstExpression 
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall 
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference 
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider 
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol 
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol 
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef 
import org.jetbrains.kotlin.fir.types.defaultType 
import org.jetbrains.kotlin.fir.visitors.FirExpressionTransformer
import org.jetbrains.kotlin.name.ClassId 
import org.jetbrains.kotlin.name.Name 

// Imports for Path operations
import kotlin.io.path.Path
import kotlin.io.path.fileName

// For KDoc reference
import com.lumos.runtime.Lumen

/**
 * Transforms FIR expressions, specifically targeting function call sites of previously
 * modified functions to inject [Lumen] object instances as arguments.
 *
 * This transformer is invoked by [LumosFirExpressionResolutionExtension] during its
 * `transformFile` phase. It relies on [LumosSessionComponent] to identify which
 * functions have had their signatures altered (i.e., which functions now expect a
 * [Lumen] parameter).
 *
 * @property session The current FIR session, used for various lookups and operations.
 * @property lumosSessionComponent The session component holding plugin configuration,
 *                                 notably the set of [LumosSessionComponent.transformedFunctionSymbols]
 *                                 which guides the identification of target call sites.
 */
class LumosCallSiteTransformer(
    val session: FirSession, 
    private val lumosSessionComponent: LumosSessionComponent
) : FirExpressionTransformer<Any?>() { // data parameter is Any?, typically null for this kind of transformation.

    /**
     * Generic visitor method for FIR elements. This overridden implementation ensures that
     * the transformer visits all children of the current [element] in the FIR tree.
     * This is standard practice for FIR transformers to guarantee full tree traversal.
     *
     * @param element The [FirElement] currently being visited.
     * @param data Contextual data passed during visitation (currently unused, defaults to `null`).
     * @return The transformed element. If the element itself is not modified, it returns
     *         the original element, but its children might have been transformed.
     */
    override fun <T : FirElement> transformElement(element: T, data: Any?): T {
        // Standard FIR transformer pattern: ensure children are visited.
        @Suppress("UNCHECKED_CAST")
        return element.transformChildren(this, data) as T
    }

    /**
     * Transforms [FirFunctionCall] expressions. This is the core method for the Lumos plugin's
     * call site modification logic.
     *
     * The transformation process involves several steps:
     * 1.  **Recursive Transformation**: It first calls `super.transformFunctionCall` to ensure
     *     that all arguments and other child expressions of the current function call are
     *     transformed. This is important because arguments themselves could be expressions
     *     requiring transformation.
     * 2.  **Target Identification**: It then resolves the symbol of the function being called
     *     (the callee). This resolved symbol is checked against the
     *     [LumosSessionComponent.transformedFunctionSymbols] set. If the symbol is present,
     *     it means the called function is one whose signature was previously modified by
     *     [LumosFunctionPatcherExtension] to include a [Lumen] parameter.
     * 3.  **Call Site Information Extraction (If Targeted)**:
     *     -   File path, file name, and line number are extracted from the function call's
     *         [FirSourceElement] using FIR APIs like [org.jetbrains.kotlin.fir.containingFileSymbol]
     *         and [org.jetbrains.kotlin.fir.FirSourceFile.getLineNumberByOffset].
     *     -   The name of the target function is extracted from its resolved symbol.
     * 4.  **[Lumen] Object Instantiation (If Targeted)**:
     *     -   The `com.lumos.runtime.Lumen` class and its primary constructor are resolved.
     *     -   FIR code is generated to create constant expressions ([FirConstExpression])
     *         for each piece of extracted call site information (filePath, fileName, etc.).
     *     -   A new [FirFunctionCall] (`lumenInstanceCall`) is built to represent the
     *         `new Lumen(...)` instantiation, using the resolved constructor and the
     *         constant expressions as arguments.
     * 5.  **Original Call Modification (If Targeted)**:
     *     -   The original function call (`transformedFunctionCall`) is reconstructed.
     *     -   A new argument list is created by taking all existing arguments from
     *         `transformedFunctionCall` and appending the `lumenInstanceCall` as the
     *         last argument.
     *     -   A new [FirFunctionCall] (`finalFunctionCall`) is built, copying all essential
     *         properties (source, callee reference, type reference, receivers, type arguments)
     *         from `transformedFunctionCall` but using the new, extended argument list.
     * 6.  **Return Value**:
     *     -   If the call was targeted and successfully modified, `finalFunctionCall` is returned,
     *         replacing the original call in the FIR tree.
     *     -   If the call was not targeted, or if any step in the modification process failed
     *         (e.g., `Lumen` class not found), the `transformedFunctionCall` (which might have
     *         had its children transformed) is returned.
     *
     * @param functionCall The [FirFunctionCall] expression to transform.
     * @param data Contextual data passed during visitation (currently unused, defaults to `null`).
     * @return The (potentially) modified [FirStatement].
     */
    override fun transformFunctionCall(functionCall: FirFunctionCall, data: Any?): FirStatement {
        // First, ensure all children (arguments, receivers) of this function call are transformed.
        val transformedFunctionCall = super.transformFunctionCall(functionCall, data) as FirFunctionCall
        
        // Resolve the symbol of the function being called.
        val resolvedSymbol = transformedFunctionCall.toResolvedCallableSymbol() as? FirFunctionSymbol<*>
        
        // Check if this call targets a function whose signature we previously modified.
        if (resolvedSymbol != null && lumosSessionComponent.transformedFunctionSymbols.contains(resolvedSymbol)) {
            // This is a call to a function that now expects a Lumen parameter.
            
            // Extract call site information.
            val source = transformedFunctionCall.source
            var filePath: String? = null
            var fileName: String? = null
            var lineNumber: Int = -1 

            if (source != null && source.kind is FirRealSourceElementKind) {
                filePath = source.path
                fileName = filePath?.let { Path(it).fileName.toString() }
                
                val firFileSymbol = source.containingFileSymbol(session)
                val firFile = firFileSymbol?.fir
                if (firFile?.sourceFile != null) {
                    lineNumber = firFile.sourceFile!!.getLineNumberByOffset(source.startOffset)
                } else {
                    // println("[Lumos WARNING] Could not get FirSourceFile for ${filePath} to determine line number accurately.")
                }
            } else {
                // println("[Lumos WARNING] Source information not available or not a real source element for call to ${resolvedSymbol.callableId}. Cannot determine file path/line.")
            }

            val targetFunctionName = resolvedSymbol.callableId.callableName.asString()
            // println("[Lumos DEBUG] Targeted Call Site Info: Path='${filePath}', File='${fileName}', Line='${lineNumber}', Target='${targetFunctionName}'")

            // Resolve Lumen class and its primary constructor.
            val lumenClassId = ClassId.fromString("com.lumos.runtime.Lumen")
            val lumenClassSymbol = session.symbolProvider.getClassLikeSymbolByClassId(lumenClassId) as? FirRegularClassSymbol
            if (lumenClassSymbol == null) {
                // println("[Lumos WARNING] Lumen class 'com.lumos.runtime.Lumen' not found. Is lumos-runtime a dependency? Aborting Lumen modification for this call site.")
                return transformedFunctionCall 
            }

            val lumenConstructorSymbol = lumenClassSymbol.declarationSymbols
                .filterIsInstance<FirConstructorSymbol>()
                .firstOrNull { it.isPrimary }
            if (lumenConstructorSymbol == null) {
                // println("[Lumos WARNING] Lumen primary constructor not found for class ${lumenClassId}. Aborting Lumen modification for this call site.")
                return transformedFunctionCall 
            }

            // Create FIR constant expressions for Lumen constructor arguments.
            val filePathArg = buildConstExpression(null, ConstantValueKind.String, filePath ?: "Unknown Path")
            val fileNameArg = buildConstExpression(null, ConstantValueKind.String, fileName ?: "Unknown File")
            val lineNumberArg = buildConstExpression(null, ConstantValueKind.Int, lineNumber)
            val targetFunctionNameArg = buildConstExpression(null, ConstantValueKind.String, targetFunctionName)

            // Build the FIR FunctionCall for Lumen instantiation.
            val lumenInstanceCall = buildFunctionCall {
                this.source = transformedFunctionCall.source // Use source of original call for the Lumen instantiation
                this.calleeReference = buildResolvedNamedReference {
                    name = lumenClassSymbol.name 
                    resolvedSymbol = lumenConstructorSymbol 
                }
                this.typeRef = buildResolvedTypeRef {
                    type = lumenClassSymbol.defaultType()
                }
                this.origin = FirFunctionCallOrigin.Regular
                this.argumentList = buildArgumentList {
                    arguments.add(filePathArg)
                    arguments.add(fileNameArg)
                    arguments.add(lineNumberArg)
                    arguments.add(targetFunctionNameArg)
                }
            }
            // println("[Lumos DEBUG] Generated Lumen instance call FIR node.")

            // Rebuild the original function call, adding the lumenInstanceCall as a new argument.
            val newArguments = ArrayList(transformedFunctionCall.argumentList.arguments) 
            newArguments.add(lumenInstanceCall) 

            val finalFunctionCall = buildFunctionCall {
                this.source = transformedFunctionCall.source
                this.calleeReference = transformedFunctionCall.calleeReference 
                this.typeRef = transformedFunctionCall.typeRef 
                this.origin = transformedFunctionCall.origin
                
                this.argumentList = buildArgumentList { arguments.addAll(newArguments) }
                
                this.dispatchReceiver = transformedFunctionCall.dispatchReceiver
                this.extensionReceiver = transformedFunctionCall.extensionReceiver
                this.explicitReceiver = transformedFunctionCall.explicitReceiver 

                this.typeArguments.addAll(transformedFunctionCall.typeArguments)
            }
            
            // println("[Lumos DEBUG] Successfully modified call to ${targetFunctionName} to include Lumen instance.")
            return finalFunctionCall 
        }
        
        // If not a targeted call, return the function call (which might have had its children transformed).
        return transformedFunctionCall
    }

    /**
     * Handles [FirQualifiedAccessExpression] expressions. This method is overridden to ensure
     * that all parts of qualified access expressions (e.g., receivers, selectors) are
     * visited and transformed by this transformer. It calls `super.transformQualifiedAccessExpression`
     * to achieve this. While Lumos primarily targets [FirFunctionCall]s, qualified accesses
     * can be components of expressions leading to function calls.
     *
     * @param qualifiedAccessExpression The qualified access expression to transform.
     * @param data Contextual data passed during visitation (currently unused, defaults to `null`).
     * @return The transformed statement.
     */
    override fun transformQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        data: Any?
    ): FirStatement {
        // Ensure children of qualified access are visited.
        return super.transformQualifiedAccessExpression(qualifiedAccessExpression, data)
    }
}
