# Project Lumos Tasks

## Phase 0: Foundation & Setup (K2 Focus)

### Story 0.1: Establish Core Project Structure & CI
Description: Setup a Gradle multi-module project configured for K2 plugin development with basic CI.
- [x] Task 0.1.1: Initialize root Gradle project using Kotlin DSL.
- [x] Task 0.1.2: Create lumos-plugin module for the K2 compiler plugin.
  - [x] Sub-task: Configure K2 plugin dependencies (e.g., kotlin-compiler-embeddable, necessary FIR artifacts). (Placeholder dependencies added)
  - [x] Sub-task: Setup manifest for CompilerPluginRegistrar.
- [x] Task 0.1.3: Create lumos-runtime module (standard Kotlin library for the Lumen data class and runtime interceptor interfaces).
- [x] Task 0.1.4: Create lumos-examples module (sample Kotlin/Java project to manually test and demonstrate the plugin).
- [x] Task 0.1.5: Create lumos-tests module for automated compiler plugin testing.
  - [x] Sub-task: Integrate kotlin-compile-testing library and configure for K2. (Initial setup, full K2 config might need refinement)
- [x] Task 0.1.6: Implement basic GitHub Actions workflow for building the project and running tests.
- [x] Task 0.1.7: Define initial versioning (e.g., 0.1.0-SNAPSHOT) and local publishing setup for inter-module dependency testing.

## Phase 1: MVP - Core Metadata Injection via FQN (K2)

### Story 1.1: Define Core Runtime Components (lumos-runtime)
Description: Create the Lumen data class.
- [ ] Task 1.1.1: (lumos-runtime) Define the initial Lumen data class (MVP fields: filePath: String, fileName: String, lineNumber: Int, targetFunctionName: String).
- [ ] Task 1.1.2: (lumos-runtime) Publish lumos-runtime locally so other modules can depend on it.

### Story 1.2: Implement K2 Plugin Entry Point & FQN Configuration Handling (lumos-plugin)
Description: Setup K2 plugin registration and the mechanism to read FQN targets from Gradle configuration.
- [ ] Task 1.2.1: (lumos-plugin) Define a Gradle extension object (e.g., LumosGradleExtension) with a property for a list of target method FQNs (e.g., ListProperty<String> targetMethodSignatures).
- [ ] Task 1.2.2: (lumos-plugin) Implement FirExtensionRegistrar for the Lumos plugin.
- [ ] Task 1.2.3: (lumos-plugin) In the FirExtensionRegistrar (or a helper class), read the FQN configuration provided via the Gradle extension.
  - [ ] Sub-task: Store this configuration where FIR transformation components can access it (e.g., in a FirSessionComponent).
- [ ] Task 1.2.4: (lumos-plugin) Implement logic to parse the configured FQN strings (e.g., com.example.MyClass.myMethod(java.lang.String,int)), potentially separating the class name, method name, and parameter types for matching.

### Story 1.3: Identify Target Functions via FQN and Modify Signatures (K2) (lumos-plugin)
Description: Match functions (Kotlin & Java) against FQN configuration and modify their FIR signatures to include the Lumen parameter.
- [ ] Task 1.3.1: (lumos-plugin) Implement a FirDeclarationGenerationExtension (or a more suitable K2 extension like FirFunctionTargetPatcher or a custom FirFunctionTransformer).
- [ ] Task 1.3.2: (lumos-plugin) Within the chosen extension, iterate through FirFunction declarations in the compilation unit.
- [ ] Task 1.3.3: (lumos-plugin) For each FirFunction, attempt to match its fully qualified name and signature against the parsed FQN targets from Story 1.2.
  - [ ] Sub-task: Ensure this matching logic works for both Kotlin-defined and Java-defined functions represented in FIR.
  - [ ] Sub-task: Handle parameter type matching carefully (e.g., mapping Java types to Kotlin types if necessary for comparison or using FIR type representations).
- [ ] Task 1.3.4: (lumos-plugin) For each matched function, modify its FirFunction representation to add a new value parameter of type Lumen.
  - [ ] Sub-task: Ensure correct resolution of the Lumen type symbol from lumos-runtime.

### Story 1.4: Transform Call Sites of FQN-Targeted Functions (K2) (lumos-plugin)
Description: Modify FIR call sites to the FQN-targeted functions to pass the Lumen object, populated with basic metadata.
- [ ] Task 1.4.1: (lumos-plugin) Implement a FirExpressionResolutionExtension or use a FIR visitor/transformer to find FirFunctionCall expressions.
- [ ] Task 1.4.2: (lumos-plugin) Identify calls that resolve to the now-transformed (FQN-targeted) functions.
- [ ] Task 1.4.3: (lumos-plugin) At each identified call site:
  - [ ] Sub-task: Extract source information (file path, line number) using FIR APIs (e.g., from FirElement.source). This needs to work for calls originating from both Kotlin and Java files.
  - [ ] Sub-task: Extract the targetFunctionName.
- [ ] Task 1.4.4: (lumos-plugin) Generate FIR code to:
  - [ ] Sub-task: Construct an instance of the Lumen data class.
  - [ ] Sub-task: Populate its fields with the extracted metadata.
- [ ] Task 1.4.5: (lumos-plugin) Modify the FirFunctionCall to include the newly created Lumen instance as an argument.

### Story 1.5: MVP Integration & End-to-End Testing (FQN Focus)
Description: Verify the FQN-based injection works for both Kotlin and Java methods in a sample project.
- [ ] Task 1.5.1: (lumos-examples) Create simple Kotlin and Java classes with methods to be targeted.
- [ ] Task 1.5.2: (lumos-examples) Configure the lumos-plugin in the example project's build script to target these methods using their FQNs.
- [ ] Task 1.5.3: (lumos-examples) Call these targeted functions from both Kotlin and Java code. Add code to print/assert the contents of the injected Lumen object.
- [ ] Task 1.5.4: (lumos-tests) Write integration tests using kotlin-compile-testing that:
  - [ ] Compile Kotlin and Java snippets with functions targeted by FQN.
  - [ ] Assert that the function signatures are changed (for both Kotlin and Java targets).
  - [ ] Assert that the Lumen parameter is accessible with correct basic data at runtime (if tests allow execution) or by inspecting generated IR/bytecode.
- [ ] Task 1.5.5: Document basic setup (plugin application, lumos-runtime dependency) and FQN configuration in README.md. Document the initial Lumen fields.

## Phase 2: Call Site Hash & Richer Lumen Data (K2)
(FQN configuration is now part of MVP; this phase focuses on enhancing Lumen.)

### Story 2.1: Implement Call Site Hash Generation
Description: Add a unique, stable hash for each call site to the Lumen object.
- [ ] Task 2.1.1: (lumos-runtime) Add callSiteHash: String to the Lumen data class.
- [ ] Task 2.1.2: (lumos-plugin) Design and implement a strategy for generating callSiteHash at the call site (e.g., hashing file path + line number + target function FQN).
- [ ] Task 2.1.3: (lumos-plugin) Populate the callSiteHash in the generated Lumen instance during call site transformation.
- [ ] Task 2.1.4: (lumos-tests) Update tests to verify the presence and basic uniqueness of callSiteHash for calls from both Kotlin and Java.

### Story 2.2: Expand Lumen Object with More Metadata
Description: Add more contextual information to the Lumen data class.
- [ ] Task 2.2.1: (lumos-plugin, lumos-runtime) Investigate and add className: String? (class containing the call site) to Lumen. Ensure this works for call sites in both Kotlin and Java files.
- [ ] Task 2.2.2: (lumos-plugin, lumos-runtime) Investigate and add callingFunctionName: String? (immediate enclosing function at call site) to Lumen.
- [ ] Task 2.2.3: (lumos-plugin, lumos-runtime) (Optional) Investigate and add moduleName: String? to Lumen if reliably accessible in K2.
- [ ] Task 2.2.4: (lumos-tests) Update tests to verify new Lumen fields. Document them.

## Phase 3: Runtime Interceptors & Passive Compile-Time Output (K2)
(These stories remain conceptually similar, now building upon FQN-only targeting.)

### Story 3.1: Implement Runtime LumenInterceptor
Description: Allow users to provide code that reacts to Lumen data at runtime.
- [ ] Task 3.1.1: (lumos-runtime) Define the LumenInterceptor interface (e.g., fun intercept(lumen: Lumen)).
- [ ] Task 3.1.2: (lumos-plugin) Implement ServiceLoader (or alternative) mechanism for discovering LumenInterceptor implementations.
- [ ] Task 3.1.3: (lumos-plugin) Modify call site transformation: after creating Lumen, generate FIR code to find and invoke all registered LumenInterceptors before calling the FQN-targeted method.
- [ ] Task 3.1.4: (lumos-examples, lumos-tests) Add examples (e.g., a logging interceptor for calls to FQN-targeted methods) and tests. Document usage.

### Story 3.2: Implement Passive Compile-Time Metadata Output
Description: Generate a file (e.g., JSON) during compilation containing data for all FQN-instrumented call sites.
- [ ] Task 3.2.1: (lumos-plugin) Add configuration options to LumosGradleExtension (e.g., outputFileEnabled, outputFilePath, outputFormat).
- [ ] Task 3.2.2: (lumos-plugin) During FIR transformation, collect Lumen data for each FQN-instrumented call site in a module-wide collection.
- [ ] Task 3.2.3: (lumos-plugin) At an appropriate point, write the collected data to the configured file (start with JSON).
- [ ] Task 3.2.4: (lumos-tests) Test file generation and content from FQN-instrumented calls. Document this feature.

## Phase 4: Active Compile-Time Processor (K2)
(This story also remains conceptually similar, building upon FQN-only targeting.)

### Story 4.1: Implement LumenBuildProcessor
Description: Allow users to run custom code during compilation that processes Lumen data (from FQN-targeted calls) as it's discovered.
- [ ] Task 4.1.1: (lumos-runtime or new lumos-build-api module) Define LumenBuildProcessor interface and LumosCompilationContext.
- [ ] Task 4.1.2: (lumos-plugin) Implement ServiceLoader (or alternative) mechanism for discovering LumenBuildProcessor implementations.
- [ ] Task 4.1.3: (lumos-plugin) Modify plugin logic: as Lumen data is determined for an FQN-instrumented call site, invoke registered LumenBuildProcessors.
- [ ] Task 4.1.4: (lumos-plugin) (Optional) Implement onCompilationModuleComplete hook.
- [ ] Task 4.1.5: (lumos-examples, lumos-tests) Add examples and tests. Document usage.

## Ongoing/General Tasks (Throughout all phases)
- [ ] Task G.1: Continuously refine error handling (e.g., for invalid FQN configurations, transformation issues) and provide clear diagnostic messages.
- [ ] Task G.2: Write KDoc for public APIs in lumos-runtime and internal documentation for lumos-plugin.
- [ ] Task G.3: Monitor K2 compiler API changes and adapt if necessary.
- [ ] Task G.4: Incrementally improve test coverage (unit, integration), ensuring good coverage for FQN targeting of both Kotlin and Java methods.
- [ ] Task G.5: Regularly update README and other documentation.
- [ ] Task G.6: Consider performance implications of FQN matching and transformations.
- [ ] Task G.7: Release Management: Setup process for publishing artifacts.
