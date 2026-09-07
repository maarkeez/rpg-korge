package shared.domain

import effect.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import shared.adapters.events.*
import unit.domain.*

class EventBusTest {
    private val eventBus: EventBus = InMemoryEventBus()

    @Test
    fun `should deliver event to subscriber when dispatched`() {
        // Given
        val unitCreated = UnitEvent.UnitCreated("unit-1")
        val receivedEvents = mutableListOf<UnitEvent.UnitCreated>()
        eventBus.subscribe<UnitEvent.UnitCreated> { receivedEvents += it }
        eventBus.publish(unitCreated)
        // When
        eventBus.dispatch()
        // Then
        assertThat(receivedEvents).containsExactly(unitCreated)
    }

    @Test
    fun `should deliver event to all subscribers when multiple subscribers are registered`() {
        // Given
        val unitCreated = UnitEvent.UnitCreated("unit-1")
        val firstReceived = mutableListOf<UnitEvent.UnitCreated>()
        val secondReceived = mutableListOf<UnitEvent.UnitCreated>()
        eventBus.subscribe<UnitEvent.UnitCreated> { firstReceived += it }
        eventBus.subscribe<UnitEvent.UnitCreated> { secondReceived += it }
        eventBus.publish(unitCreated)
        // When
        eventBus.dispatch()
        // Then
        assertThat(firstReceived).containsExactly(unitCreated)
        assertThat(secondReceived).containsExactly(unitCreated)
    }

    @Test
    fun `should not deliver event to subscriber of other event type when dispatched`() {
        // Given
        val unitCreated = UnitEvent.UnitCreated("unit-1")
        val effectCreated = mutableListOf<EffectEvent.EffectCreated>()
        eventBus.subscribe<EffectEvent.EffectCreated> { effectCreated += it }
        eventBus.publish(unitCreated)
        // When
        eventBus.dispatch()
        // Then
        assertThat(effectCreated).isEmpty()
    }

    @Test
    fun `should not deliver event when subscription is disposed`() {
        // Given
        val unitCreated = UnitEvent.UnitCreated("unit-1")
        val receivedEvents = mutableListOf<UnitEvent.UnitCreated>()
        val subscription = eventBus.subscribe<UnitEvent.UnitCreated> { receivedEvents += it }
        subscription.dispose()
        eventBus.publish(unitCreated)
        // When
        eventBus.dispatch()
        // Then
        assertThat(receivedEvents).isEmpty()
    }

    @Test
    fun `should dispatch events in the order they were published`() {
        // Given
        val firstUnitCreated = UnitEvent.UnitCreated("unit-1")
        val secondUnitCreated = UnitEvent.UnitCreated("unit-2")
        val receivedEvents = mutableListOf<UnitEvent.UnitCreated>()
        eventBus.subscribe<UnitEvent.UnitCreated> { receivedEvents += it }
        eventBus.publish(firstUnitCreated)
        eventBus.publish(secondUnitCreated)
        // When
        eventBus.dispatch()
        // Then
        assertThat(receivedEvents).containsExactly(firstUnitCreated, secondUnitCreated)
    }
}
