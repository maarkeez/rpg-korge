package com.mkz.rpg.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlin.test.Test

/**
 * A presentation Api takes every collaborator from its caller, so each scene shows all of its wiring: its primary
 * constructor is its only one and none of its parameters has a default value (see docs/architecture/package-structure.md).
 */
class ApiConstructorArchitectureTest {
    // The presentation Apis, e.g.: `BattlefieldApi`, and the nested `BattlefieldHudApi`.
    private val apis =
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { it.resideInPackage("..adapters.presentation..") }
            .filter { it.isTopLevel }
            .filter { it.name.endsWith("Api") }

    @Test
    fun `should not have default values when they are the constructor parameters of an Api`() {
        apis.shouldNotBeEmpty()
        assertSoftly {
            apis.forEach { api ->
                val defaulted =
                    api.primaryConstructor
                        ?.parameters
                        .orEmpty()
                        .filter { parameter -> parameter.hasDefaultValue() }
                        .map { parameter -> parameter.name }

                withClue("Api '${api.name}' must take these parameters from its caller instead of defaulting them: $defaulted") {
                    defaulted.shouldBeEmpty()
                }
            }
        }
    }

    @Test
    fun `should only have its primary constructor when it is an Api`() {
        assertSoftly {
            apis.forEach { api ->
                withClue("Api '${api.name}' must only have its primary constructor") {
                    api.secondaryConstructors.shouldBeEmpty()
                }
            }
        }
    }
}
