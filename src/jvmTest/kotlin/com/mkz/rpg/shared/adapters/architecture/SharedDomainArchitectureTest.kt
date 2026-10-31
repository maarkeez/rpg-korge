package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

/**
 * Every subdomain may depend on `shared`, so its domain holds only the plumbing of the event bus and the save, never a
 * game rule: a rule belongs to the subdomain owning its concept (e.g.: the gold cap to the inventory), which hands it to
 * the others through a query. See docs/architecture/package-structure.md.
 */
class SharedDomainArchitectureTest {
    private val sharedDomainPackage = "com.mkz.rpg.shared.domain"

    // subscribe: the reified extension of EventBus.
    private val plumbing = listOf("DomainEvent", "EventBus", "subscribe", "Subscription", "Snapshottable")

    @Test
    fun `should only declare the plumbing of the event bus and the save when it is the shared domain package`() {
        // Given
        val files = Konsist.scopeFromProduction().files.filter { file -> file.packagee?.fullyQualifiedName == sharedDomainPackage }
        // When
        val declarations =
            files.flatMap { file ->
                file.classes(includeNested = false).map { it.name } +
                    file.interfaces(includeNested = false).map { it.name } +
                    file.objects(includeNested = false).map { it.name } +
                    file.functions(includeNested = false, includeLocal = false).map { it.name } +
                    file.properties(includeNested = false, includeLocal = false).map { it.name } +
                    file.typeAliases.map { it.name }
            }
        // Then
        withClue("$sharedDomainPackage declares $declarations. Move a game rule to the subdomain owning its concept instead.") {
            declarations shouldContainExactlyInAnyOrder plumbing
        }
    }
}
