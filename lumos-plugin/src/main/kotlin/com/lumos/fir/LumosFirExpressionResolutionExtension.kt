package com.lumos.fir

import org.jetbrains.kotlin.fir.FirFile
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExpressionResolutionExtension
import org.jetbrains.kotlin.fir.extensions.FirFileTransformerExtension

/**
 * FIR extension that serves a dual role in the Lumos plugin pipeline:
 * 1.  As a [FirExpressionResolutionExtension]: Although not actively modifying expression
 *     resolution in its current form, it's registered as such and owns the
 *     [LumosCallSiteTransformer]. Future enhancements might leverage its resolution capabilities.
 * 2.  As a [FirFileTransformerExtension]: This is its primary active role. It enables
 *     the transformation of entire FIR files by applying the [LumosCallSiteTransformer]
 *     to traverse and modify expressions within each file.
 *
 * This extension is registered by [LumosFirExtensionRegistrar].
 *
 * @property session The current FIR session, used for initializing components and transformers.
 */
class LumosFirExpressionResolutionExtension(session: FirSession) : 
    FirExpressionResolutionExtension(session),
    FirFileTransformerExtension {

    /**
     * The session component that holds configuration for the Lumos plugin.
     * It provides access to data such as the list of parsed FQN targets and the set of
     * symbols for functions whose signatures have been transformed. This information is
     * crucial for the [callSiteTransformer].
     */
    private val lumosSessionComponent: LumosSessionComponent = session.registrations.findRequire(LumosSessionComponent::class)
    
    /**
     * An instance of [LumosCallSiteTransformer], responsible for the actual modification
     * of function call sites. This transformer is initialized with the current [session]
     * and the [lumosSessionComponent] to enable it to identify target calls and
     * generate necessary FIR code for [com.lumos.runtime.Lumen] instantiation.
     */
    private val callSiteTransformer = LumosCallSiteTransformer(session, lumosSessionComponent)

    /**
     * Transforms the given [FirFile] by applying the [callSiteTransformer] to it.
     * This method is invoked by the Kotlin compiler for each file being compiled.
     * The [LumosCallSiteTransformer], being a [org.jetbrains.kotlin.fir.visitors.FirTransformer],
     * will traverse the FIR tree of the file. Its overridden methods (especially
     * `transformFunctionCall`) will then identify and modify call sites of targeted functions.
     *
     * @param file The [FirFile] to be transformed.
     * @param data Contextual data passed during the transformation process (currently unused, defaults to `null`).
     * @return The transformed [FirFile]. The transformations are applied in-place by the
     *         underlying visitor, but returning the file is part of the extension contract.
     */
    override fun transformFile(file: FirFile, data: Any?): FirFile {
        // Delegate the transformation of the entire file to the callSiteTransformer.
        // LumosCallSiteTransformer extends FirExpressionTransformer, which inherits transformFile
        // from FirTransformer. This will effectively traverse the file and apply the
        // overridden transformFunctionCall (and other transformXYZ methods) from LumosCallSiteTransformer.
        return callSiteTransformer.transformFile(file, data)
    }

    // TODO for FirExpressionResolutionExtension part:
    // Determine if any specific overrides from FirExpressionResolutionExtension are necessary
    // for future features. For now, its registration primarily facilitates the
    // FirFileTransformerExtension aspect and the lifecycle management of callSiteTransformer.
}
