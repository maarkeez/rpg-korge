package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

class SnapshotCoverageArchitectureTest {
    private val rootPackage = "com.mkz.rpg"

    private val whyNotSaved: Map<String, String> =
        mapOf(
            "ability" to "read from the resources",
            "building" to "read from the resources",
            "effect" to "read from the resources",
            "lootTable" to "read from the resources",
            "lootableItem" to "read from the resources",
            "player" to "read from the resources",
            "terrain" to "read from the resources",
            "unit" to "read from the resources",
            "battle" to "no battle is in progress while exploring, the only time the game is saved",
            "cpuBattleBrain" to "no battle is in progress while exploring, the only time the game is saved",
            "localization" to "the language is picked for the session",
            "telemetry" to "the consent and the install id are kept on the device, for every game",
            "worldBuilder" to "the world builder exports its worlds instead",
            "storybook" to "a tool, not the game",
            "screen.adapters.presentation.selection" to "what the HUD displays, drawn again from the other subdomains",
        )

    private val scope = Konsist.scopeFromProduction()

    private val subdomainsWithRepository: List<String> =
        scope
            .interfaces()
            .filter { declaration -> declaration.name.endsWith("Repository") && declaration.packagee?.fullyQualifiedName?.endsWith(".domain") == true }
            .mapNotNull { declaration ->
                declaration.packagee
                    ?.fullyQualifiedName
                    ?.removePrefix("$rootPackage.")
                    ?.removeSuffix(".domain")
            }.filterNot { subdomain -> subdomain == "save" }
            .distinct()

    @Test
    fun `should be snapshottable when its subdomain holds runtime state`() {
        // When
        val missing =
            subdomainsWithRepository
                .filterNot { subdomain -> subdomain in whyNotSaved }
                .filterNot { subdomain ->
                    val name = subdomain.substringAfterLast(".").replaceFirstChar(Char::uppercase) + "Snapshottable"
                    scope.classes().any { declaration ->
                        declaration.name == name &&
                            declaration.packagee?.fullyQualifiedName == "$rootPackage.$subdomain.adapters.save" &&
                            declaration.parents.any { parent -> parent.name == "Snapshottable" }
                    }
                }
        // Then
        subdomainsWithRepository.shouldNotBeEmpty()
        withClue("Subdomains holding runtime state that are not saved: $missing. Add a `<Subdomain>Snapshottable` in their adapters.save, or list them as not saved, with why.") {
            missing.shouldBeEmpty()
        }
    }

    @Test
    fun `should hold a repository when it is listed as not saved`() {
        // When
        val unknown = whyNotSaved.keys.filterNot { subdomain -> subdomain in subdomainsWithRepository }
        // Then
        withClue("Subdomains listed as not saved that hold no repository: $unknown. Remove them from the list.") {
            unknown.shouldBeEmpty()
        }
    }
}
