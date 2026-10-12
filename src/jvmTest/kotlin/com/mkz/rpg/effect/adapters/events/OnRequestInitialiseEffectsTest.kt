package com.mkz.rpg.effect.adapters.events

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.usecases.commands.InitialiseEffects
import com.mkz.rpg.effect.usecases.services.FakeEffectLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OnRequestInitialiseEffectsTest {
    private val eventBus = InMemoryEventBus()
    private val effectRepository = InMemoryEffectRepository()
    private val venom = decreaseHealthEffect(id = "venom").toDto()
    private val onRequestInitialiseEffects = OnRequestInitialiseEffects(InitialiseEffects(FakeEffectLoader(effects = listOf(venom)), effectRepository, eventBus), eventBus)

    @Test
    fun `should initialise the effects when the request is dispatched`() {
        // Given
        eventBus.publish(EffectEvent.RequestInitialiseEffects)
        // When
        eventBus.dispatch()
        // Then
        effectRepository.searchById("venom")?.toDto() shouldBe venom
    }
}
