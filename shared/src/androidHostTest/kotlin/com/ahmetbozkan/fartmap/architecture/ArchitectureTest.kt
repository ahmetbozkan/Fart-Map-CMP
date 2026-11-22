package com.ahmetbozkan.fartmap.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Architecture rules from planning/architecture/Architecture.md §3–§5, enforced on every Kotlin file of the project
 * (all modules, production code only). Runs on the JVM host: `./gradlew :shared:testAndroidHostTest`.
 * Rules pass vacuously while the codebase is empty and start biting as soon as matching code exists.
 */
class ArchitectureTest {

    private val root = "com.ahmetbozkan.fartmap"
    private val firebasePrefixes = listOf("dev.gitlive", "cocoapods.FirebaseCore", "com.google.firebase")
    private val extendsStateViewModel = Regex(""":\s*StateViewModel<""")

    private val files: List<KoFileDeclaration> by lazy {
        Konsist.scopeFromProduction().files.filterNot { it.unixPath.contains("/.claude/") }
    }

    private val KoFileDeclaration.unixPath: String get() = path.replace('\\', '/')
    private val KoFileDeclaration.pkg: String get() = packagee?.name.orEmpty()
    private val KoFileDeclaration.importNames: List<String> get() = imports.map { it.name }

    private fun featureOf(name: String): String? =
        Regex("""^${Regex.escape(root)}\.feature\.([a-z0-9_]+)(\.|$)""").find(name)?.groupValues?.get(1)

    private fun isLayer(pkg: String, layer: String) =
        pkg.contains(".$layer.") || pkg.endsWith(".$layer")

    private fun assertNoViolations(rule: String, violations: List<String>) =
        assertTrue(violations.isEmpty(), "$rule\n" + violations.joinToString("\n") { " - $it" })

    @Test
    fun `domain imports nothing from data, presentation, Room, Firebase or platform SDKs`() {
        val forbidden = listOf(".data.", ".presentation.", "androidx.room", "dev.gitlive", "android.", "platform.")
        val violations = files.filter { it.pkg.startsWith(root) && isLayer(it.pkg, "domain") }.flatMap { file ->
            file.importNames.filter { imp -> forbidden.any { imp.contains(it) || imp.startsWith(it.trimStart('.')) } }
                .map { "${file.unixPath}: imports $it" }
        }
        assertNoViolations("Domain must stay pure (Architecture.md §3)", violations)
    }

    @Test
    fun `presentation never imports data, DTOs, Room or Firebase`() {
        val violations = files.filter { it.pkg.startsWith(root) && isLayer(it.pkg, "presentation") }.flatMap { file ->
            file.importNames.filter { imp ->
                imp.contains(".data.") || imp.startsWith("androidx.room") || imp.startsWith("dev.gitlive") ||
                    imp.startsWith("$root.firebase")
            }.map { "${file.unixPath}: imports $it" }
        }
        assertNoViolations("Presentation must not see the data layer (Architecture.md §3)", violations)
    }

    @Test
    fun `features never import other features`() {
        val violations = files.mapNotNull { file -> featureOf(file.pkg)?.let { file to it } }.flatMap { (file, own) ->
            file.importNames
                .mapNotNull { imp -> featureOf(imp) }
                .filter { it != own }
                .map { "${file.unixPath}: $own imports feature.$it" }
        }
        assertNoViolations("Features communicate only through core (Architecture.md §3)", violations)
    }

    @Test
    fun `Firebase SDK types appear only in firebaseData`() {
        val violations = files.filterNot { it.unixPath.contains("/firebaseData/") }.flatMap { file ->
            file.importNames
                .filter { imp -> firebasePrefixes.any { imp.startsWith(it) } }
                .map { "${file.unixPath}: imports $it" }
        }
        assertNoViolations("Firebase lives only in :firebaseData (ADR-0021)", violations)
    }

    @Test
    fun `repository interfaces live in domain and implementations in data or firebaseData`() {
        val interfaces = files.flatMap { f -> f.interfaces().map { f to it.name } }
            .filter { (_, name) -> name.endsWith("Repository") }
            .filterNot { (f, _) -> isLayer(f.pkg, "domain") }
            .map { (f, name) -> "${f.unixPath}: interface $name is not in a domain package" }
        val impls = files.flatMap { f -> f.classes().map { f to it.name } }
            .filter { (_, name) -> name.endsWith("RepositoryImpl") }
            .filterNot { (f, _) -> isLayer(f.pkg, "data") || f.pkg.startsWith("$root.firebase") }
            .map { (f, name) -> "${f.unixPath}: class $name is not in a data/firebase package" }
        assertNoViolations("Repository placement (Architecture.md §5)", interfaces + impls)
    }

    @Test
    fun `screen ViewModels extend StateViewModel`() {
        val violations = files.flatMap { f -> f.classes().map { f to it } }
            .filter { (_, c) -> c.name.endsWith("ViewModel") && c.name != "StateViewModel" }
            .filterNot { (_, c) -> c.text.contains(extendsStateViewModel) }
            .map { (f, c) -> "${f.unixPath}: ${c.name} does not extend StateViewModel (ADR-0004)" }
        assertNoViolations("One presentation pattern (ADR-0004)", violations)
    }

    @Test
    fun `no printStackTrace anywhere`() {
        val violations = files.filter { it.text.contains(".printStackTrace(") }.map { it.unixPath }
        assertNoViolations("Errors are never swallowed (CLAUDE.md non-negotiables)", violations)
    }
}
