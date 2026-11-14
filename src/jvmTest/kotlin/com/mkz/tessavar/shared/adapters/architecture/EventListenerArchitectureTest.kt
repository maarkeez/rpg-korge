package com.mkz.tessavar.shared.adapters.architecture

import com.lemonappdev.konsist.api.Konsist
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EventListenerArchitectureTest {
    private val productionClasses = Konsist.scopeFromProduction().classes()

    private val commandNames =
        productionClasses
            .filter { it.resideInPackage("..usecases.commands..") }
            .filter { it.isTopLevel }
            .map { it.name }
            .toSet()

    private val listeners =
        productionClasses
            .filter { it.resideInPackage("..adapters.events..") }
            .filter { it.isTopLevel }
            .filter { it.name.startsWith("On") }

    @Test
    fun `should only depend on command use cases and the event bus when it is an event listener`() {
        listeners.shouldNotBeEmpty()
        assertSoftly {
            listeners.forEach { listener ->
                val otherDependencies =
                    listener.primaryConstructor
                        ?.parameters
                        .orEmpty()
                        .map { parameter -> parameter.type.name }
                        .filterNot { typeName -> typeName == "EventBus" || typeName in commandNames }

                withClue("Listener '${listener.name}' must only depend on command use cases and the EventBus, found: $otherDependencies") {
                    otherDependencies.shouldBeEmpty()
                }
            }
        }
    }

    @Test
    fun `should not publish events when it is an event listener`() {
        assertSoftly {
            listeners.forEach { listener ->
                withClue("Listener '${listener.name}' must not publish events: a command use case publishes them") {
                    listener.text.contains(".publish(") shouldBe false
                }
            }
        }
    }
}
