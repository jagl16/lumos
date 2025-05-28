package com.lumos.fir

import com.lumos.fqn.ParsedFqnData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.extensions.FirFunctionTargetPatcher
import org.jetbrains.kotlin.fir.extensions.FirFunctionTargetPatcherExtension
import org.jetbrains.kotlin.fir.extensions.FirFunctionTargetMatcher
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toFirClassLikeSymbol // Added import for annotation resolution
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.targets.FirFunctionTarget
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

// For KDoc reference
import com.lumos.runtime.Lumen

/**
 * FIR extension responsible for patching the signatures of functions targeted by FQN or annotation.
 * This extension identifies functions based on the configurations provided via [LumosSessionComponent]
 * or by the presence of the `@LumosMaxima` annotation. It then modifies their FIR representation
 * to include a [Lumen] parameter. It is registered by [LumosFirExtensionRegistrar].
 * @property session The current FIR session, used for various lookups and operations.
 */
class LumosFunctionPatcherExtension(session: FirSession) : FirFunctionTargetPatcherExtension(session) {

    /**
     * The session component that holds configuration for the Lumos plugin.
     * It provides the list of parsed FQN targets ([LumosSessionComponent.parsedFqnTargets])
     * and stores the symbols of functions whose signatures have been transformed
     * ([LumosSessionComponent.transformedFunctionSymbols]).
     */
    private val lumosSessionComponent: LumosSessionComponent = session.registrations.findRequire(LumosSessionComponent::class)

    /**
     * Normalizes a type string by removing common prefixes like "kotlin." and "java.lang.".
     * This aids in comparing type strings derived from FIR (which are fully qualified)
     * with type strings from parsed FQN configurations (which might be less specific).
     *
     * @param typeStr The type string to normalize.
     * @return The normalized type string.
     */
    private fun normalizeTypeString(typeStr: String): String {
        return typeStr.replace("kotlin.", "").replace("java.lang.", "")
    }

    /**
     * Returns a [FirFunctionTargetMatcher] that identifies functions to be patched
     * for Lumen object injection.
     *
     * The matching process prioritizes the `@com.lumos.runtime.LumosMaxima` annotation:
     * 1. If a function is annotated with `@com.lumos.runtime.LumosMaxima`, it is considered a target.
     * 2. If the annotation is not present, the matcher falls back to checking against
     *    the list of Fully Qualified Names (FQNs) provided via the Gradle extension
     *    (and stored in [LumosSessionComponent.parsedFqnTargets]). This FQN matching
     *    includes verifying the package name, class name (if applicable), method name,
     *    parameter count, and the sequence and normalized types of its parameters.
     *
     * @return A [FirFunctionTargetMatcher] instance for identifying target functions.
     */
    override fun getMatcher(): FirFunctionTargetMatcher {
        return FirFunctionTargetMatcher { functionSymbol, firSession -> // firSession is the same as this.session
            val firFunction = functionSymbol.fir 

            // 1. Check for @LumosMaxima annotation
            val lumosMaximaAnnotationClassId = ClassId.fromString("com.lumos.runtime.LumosMaxima")
            val hasLumosMaximaAnnotation = firFunction.annotations.any { annotationCall ->
                // Resolve the type of the annotation call to get its ClassId
                val annotationClassSymbol = annotationCall.annotationTypeRef.toFirClassLikeSymbol(this.session) // Use this.session
                annotationClassSymbol?.classId == lumosMaximaAnnotationClassId
            }

            if (hasLumosMaximaAnnotation) {
                // For debugging (can be removed later):
                // println("[Lumos DEBUG] Matched function by @LumosMaxima: ${functionSymbol.callableId.asString()}")
                return@FirFunctionTargetMatcher true // Annotated, so it's a target
            }

            // 2. If not annotated, proceed with existing FQN matching logic
            val callableId = functionSymbol.callableId
            // val firFunction is already available from above.
            val currentFunctionContextFqn = callableId.classId?.asSingleFqName()?.asString() 
                                            ?: callableId.packageName.asString()
            val currentFunctionName = callableId.callableName.asString()

            val fqnMatch = lumosSessionComponent.parsedFqnTargets.any { parsedFqn ->
                val namesMatch = parsedFqn.classFqn == currentFunctionContextFqn &&
                                 parsedFqn.methodName == currentFunctionName
                
                if (!namesMatch) {
                    false 
                } else {
                    if (firFunction.valueParameters.size != parsedFqn.parameterTypes.size) {
                        false 
                    } else {
                        if (parsedFqn.parameterTypes.isEmpty()) {
                            true 
                        } else {
                            val firParamTypesNormalized = firFunction.valueParameters.map { param ->
                                normalizeTypeString(param.returnTypeRef.coneType.renderReadable())
                            }
                            val fqnParamTypesNormalized = parsedFqn.parameterTypes.map { normalizeTypeString(it) }
                            
                            firParamTypesNormalized == fqnParamTypesNormalized
                        }
                    }
                }
            }
            
            if (fqnMatch) {
                // For debugging (can be removed later):
                // println("[Lumos DEBUG] Matched function by FQN: ${functionSymbol.callableId.asString()}")
            }
            fqnMatch // Return the result of FQN matching
        }
    }

    /**
     * Returns a [FirFunctionTargetPatcher] responsible for modifying the matched function targets.
     * The returned patcher specifically adds a [Lumen] parameter to the function signature.
     *
     * @return A [FirFunctionTargetPatcher] instance that performs the function modification.
     */
    override fun getPatcher(): FirFunctionTargetPatcher {
        /**
         * An anonymous implementation of [FirFunctionTargetPatcher] that modifies a given
         * [FirFunctionTarget] to include a [Lumen] parameter.
         * This is the core logic for altering function signatures within the Lumos plugin.
         */
        return object : FirFunctionTargetPatcher(session) {
            /**
             * Modifies the provided [target] (a [FirFunctionTarget]) by adding a [Lumen] parameter
             * to its underlying [FirSimpleFunction].
             *
             * The process involves:
             * 1. Retrieving the original [FirSimpleFunction] from the target.
             * 2. Resolving the [com.lumos.runtime.Lumen] class type within the current FIR session.
             *    If resolution fails, the original target is returned unmodified.
             * 3. Creating a new [FirValueParameter] representing the `lumen: Lumen` parameter.
             *    This parameter is marked with [LumosPluginDeclarationOrigin].
             * 4. Constructing a new [FirSimpleFunction] by copying all properties from the original
             *    function and appending the new `lumen` parameter to its value parameter list.
             *    The new function is also marked with [LumosPluginDeclarationOrigin].
             *    A new [FirNamedFunctionSymbol] is created for the new function, using the
             *    `CallableId` of the original function's symbol.
             * 5. Storing the symbol of the newly created (modified) function in
             *    [LumosSessionComponent.transformedFunctionSymbols] for later identification by
             *    call site transformers.
             * 6. Returning a new [FirFunctionTarget] that points to the modified function's symbol.
             *
             * @param target The function target to modify.
             * @return A new [FirFunctionTarget] referencing the modified function, or the original
             *         [target] if modification was not possible or applicable (e.g., not a [FirSimpleFunction]
             *         or if [Lumen] type resolution fails).
             */
            override fun modifyTarget(target: FirFunctionTarget): FirFunctionTarget {
                val originalFunctionSymbol = target.functionSymbol ?: return target
                val originalFunction = originalFunctionSymbol.fir

                if (originalFunction !is FirSimpleFunction) {
                    return target
                }

                val lumenClassId = ClassId.fromString("com.lumos.runtime.Lumen")
                val lumenClassSymbol = session.symbolProvider.getClassLikeSymbolByClassId(lumenClassId) as? FirRegularClassSymbol
                
                if (lumenClassSymbol == null) {
                    return target
                }
                
                val lumenConeType = lumenClassSymbol.defaultType()

                val lumenParameterSymbol = FirValueParameterSymbol()
                val lumenParameter = buildValueParameter {
                    moduleData = session.moduleData
                    origin = LumosPluginDeclarationOrigin 
                    returnTypeRef = buildResolvedTypeRef { type = lumenConeType }
                    name = Name.identifier("lumen") 
                    symbol = lumenParameterSymbol
                    containingFunctionSymbol = originalFunctionSymbol 
                    isCrossinline = false
                    isNoinline = false
                    isVararg = false
                }

                val newFunctionSymbol = FirNamedFunctionSymbol(originalFunctionSymbol.callableId)

                val newFunction = buildSimpleFunction {
                    moduleData = originalFunction.moduleData
                    source = originalFunction.source 
                    this.session = this@LumosFunctionPatcherExtension.session 
                    
                    origin = LumosPluginDeclarationOrigin 
                    status = originalFunction.status 
                    returnTypeRef = originalFunction.returnTypeRef 
                    
                    originalFunction.receiverParameter?.let { this.receiverParameter = it }
                    
                    contextReceivers.addAll(originalFunction.contextReceivers)
                    
                    name = originalFunction.name 
                    symbol = newFunctionSymbol 

                    valueParameters.addAll(originalFunction.valueParameters)
                    valueParameters.add(lumenParameter)

                    typeParameters.addAll(originalFunction.typeParameters)
                    body = originalFunction.body ?: buildBlock {} 
                    deprecationsProvider = originalFunction.deprecationsProvider
                    attributes = originalFunction.attributes.copy()
                }
                
                lumosSessionComponent.transformedFunctionSymbols.add(newFunctionSymbol)
                
                return FirFunctionTarget(target.labeledElement, newFunctionSymbol)
            }
        }
    }
}
