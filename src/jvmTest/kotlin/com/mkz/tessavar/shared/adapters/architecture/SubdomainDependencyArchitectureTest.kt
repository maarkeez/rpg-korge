package com.mkz.tessavar.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class SubdomainDependencyArchitectureTest {
    private val rootPackage = "com.mkz.tessavar"

    private val allowedDependencies: Map<String, Set<String>> =
        mapOf(
            "shared" to emptySet(),
            "effect" to setOf("shared"),
            "localization" to setOf("shared"),
            "player" to setOf("shared"),
            "save" to setOf("shared"),
            "terrain" to setOf("shared"),
            "unit" to setOf("shared"),
            "ability" to setOf("shared", "effect"),
            "lootableItem" to setOf("shared", "effect"),
            "battle" to setOf("shared"),
            "lootTable" to setOf("shared", "lootableItem"),
            "building" to setOf("shared", "battlefield"),
            "quest" to setOf("shared", "battlefield", "battleUnit", "party", "inventory"),
            "battlefield" to setOf("shared", "effect", "terrain", "battle"),
            "party" to setOf("shared", "effect", "player", "battle", "battlefield", "battleUnit"),
            "battleUnit" to setOf("shared", "effect", "player", "unit", "ability", "battle", "battlefield"),
            "telemetry" to setOf("shared", "battle", "inventory", "party", "quest"),
        )

    private val imports: List<SubdomainImport> =
        Konsist
            .scopeFromProduction()
            .files
            .flatMap { file ->
                val subdomain = file.packagee?.fullyQualifiedName?.let(::subdomainOf) ?: return@flatMap emptyList()
                file.imports.mapNotNull { import -> subdomainOf(import.name)?.let { importedSubdomain -> SubdomainImport(subdomain, importedSubdomain, file.name) } }
            }.filter { import -> import.subdomain != import.importedSubdomain }

    private val dependencies: Set<Pair<String, String>> = imports.map { import -> import.subdomain to import.importedSubdomain }.toSet()

    private val subdomains: Set<String> = dependencies.flatMap { (subdomain, importedSubdomain) -> listOf(subdomain, importedSubdomain) }.toSet()

    private val cycles: Set<Set<String>> =
        subdomains
            .associateWith(::reachableFrom)
            .let { reachable -> subdomains.map { subdomain -> subdomains.filter { other -> other in reachable.getValue(subdomain) && subdomain in reachable.getValue(other) }.toSet() } }
            .filter { cycle -> cycle.size > 1 }
            .toSet()

    @Test
    fun `should not depend on each other when they are subdomains, even indirectly`() {
        withClue("Subdomains that depend on each other: ${cycles.map { it.sorted().joinToString(prefix = "{", postfix = "}") }}. Publish a request event of the upstream subdomain instead.") {
            cycles.shouldBeEmpty()
        }
    }

    @Test
    fun `should only depend on the allowed subdomains when they are restricted`() {
        val forbiddenImports =
            imports
                .filter { import -> allowedDependencies[import.subdomain]?.contains(import.importedSubdomain) == false }
                .map { import -> "${import.subdomain} → ${import.importedSubdomain} (${import.fileName})" }
                .distinct()
                .sorted()
        withClue("Restricted subdomains that depend on subdomains not allowed for them: $forbiddenImports. Publish a request event of the upstream subdomain instead.") {
            forbiddenImports.shouldBeEmpty()
        }
    }

    @Test
    fun `should depend on every allowed subdomain when they are restricted`() {
        val unusedDependencies =
            allowedDependencies
                .flatMap { (subdomain, allowed) -> allowed.filter { allowedSubdomain -> subdomain to allowedSubdomain !in dependencies }.map { allowedSubdomain -> "$subdomain → $allowedSubdomain" } }
                .sorted()
        withClue("Allowed dependencies that are no longer used: $unusedDependencies. Remove them from the allowed dependencies.") {
            unusedDependencies.shouldBeEmpty()
        }
    }

    private data class SubdomainImport(
        val subdomain: String,
        val importedSubdomain: String,
        val fileName: String,
    )

    private fun reachableFrom(subdomain: String): Set<String> {
        val reached = mutableSetOf<String>()
        val pending = ArrayDeque(listOf(subdomain))
        while (pending.isNotEmpty()) {
            val current = pending.removeFirst()
            dependencies
                .filter { (from, _) -> from == current }
                .map { (_, to) -> to }
                .filter(reached::add)
                .forEach(pending::addLast)
        }
        return reached
    }

    private fun subdomainOf(name: String): String? =
        name
            .removePrefix("$rootPackage.")
            .takeIf { it != name }
            ?.substringBefore(".")
            ?.takeIf { it.isNotEmpty() }
}
