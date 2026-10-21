package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

/**
 * Two subdomains must not depend on each other: the upstream one declares `Request…` events, which the downstream one
 * publishes, instead of the upstream one depending on it (see docs/architecture/package-structure.md). The subdomains
 * are read as in the dependency report (docs/architecture/dependencies.md).
 */
class SubdomainDependencyArchitectureTest {
    // The two-way dependencies still to be removed. Only ever remove a pair from this list.
    private val knownTwoWayDependencies =
        setOf(
            setOf("battle", "cpuBattleBrain"),
            setOf("battlefield", "inventory"),
            setOf("battlefield", "npc"),
            setOf("cpuBattleBrain", "cpuWanderBrain"),
            setOf("npc", "party"),
        )

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

    private val twoWayDependencies: Set<Set<String>> =
        dependencies
            .filter { (subdomain, importedSubdomain) -> (importedSubdomain to subdomain) in dependencies }
            .map { (subdomain, importedSubdomain) -> setOf(subdomain, importedSubdomain) }
            .toSet()

    @Test
    fun `should not depend on each other when they are two subdomains`() {
        val newTwoWayDependencies = twoWayDependencies - knownTwoWayDependencies

        withClue("Subdomains that depend on each other: ${newTwoWayDependencies.map { it.joinToString(" ⇄ ") }}. Publish a request event of the upstream subdomain instead.") {
            newTwoWayDependencies.shouldBeEmpty()
        }
    }

    @Test
    fun `should only list the two-way dependencies that still exist when they are known`() {
        val removedTwoWayDependencies = knownTwoWayDependencies - twoWayDependencies

        withClue("These subdomains no longer depend on each other, remove them from the known two-way dependencies: ${removedTwoWayDependencies.map { it.joinToString(" ⇄ ") }}") {
            removedTwoWayDependencies.shouldBeEmpty()
        }
    }

    // The first package segment after the root package, e.g.: `screen` for `com.mkz.rpg.screen.adapters.presentation`.
    // Null out of the root package (e.g.: `korlibs`), and for the root package itself (e.g.: `Main.kt`).
    private fun subdomainOf(name: String): String? = name.removePrefix("$rootPackage.").takeIf { it != name }?.substringBefore(".")?.takeIf { it.isNotEmpty() }
}
