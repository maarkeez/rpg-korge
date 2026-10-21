package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

/**
 * Subdomains must not depend on each other, even indirectly (e.g.: `a` → `b` → `c` → `a`): the upstream one declares
 * `Request…` events, which the downstream one publishes, instead of the upstream one depending on it (see
 * docs/architecture/package-structure.md). The subdomains and their cycles are read as in the dependency report
 * (docs/architecture/dependencies.md).
 */
class SubdomainDependencyArchitectureTest {
    private val rootPackage = "com.mkz.rpg"

    private val dependencies: Set<Pair<String, String>> =
        Konsist
            .scopeFromProduction()
            .files
            .flatMap { file ->
                val subdomain = file.packagee?.fullyQualifiedName?.let(::subdomainOf) ?: return@flatMap emptyList()
                file.imports.mapNotNull { import -> subdomainOf(import.name)?.let { importedSubdomain -> subdomain to importedSubdomain } }
            }.filter { (subdomain, importedSubdomain) -> subdomain != importedSubdomain }
            .toSet()

    private val subdomains: Set<String> = dependencies.flatMap { (subdomain, importedSubdomain) -> listOf(subdomain, importedSubdomain) }.toSet()

    // The groups of subdomains that can reach each other, even indirectly (strongly connected components), with more than one subdomain.
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

    // The subdomains the given one depends on, directly or not.
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

    // The first package segment after the root package, e.g.: `screen` for `com.mkz.rpg.screen.adapters.presentation`.
    // Null out of the root package (e.g.: `korlibs`), and for the root package itself (e.g.: `Main.kt`).
    private fun subdomainOf(name: String): String? =
        name
            .removePrefix("$rootPackage.")
            .takeIf { it != name }
            ?.substringBefore(".")
            ?.takeIf { it.isNotEmpty() }
}
