package com.mkz.rpg.effect.usecases.commands

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectEvent.EffectCreated
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.teleportEffect
import com.mkz.rpg.effect.usecases.services.EffectLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseEffectsTest {
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()
    private val effectLoader: EffectLoader = mock()
    private val initialiseEffects = InitialiseEffects(effectLoader, effectRepository, eventBus)

    @Test
    fun `should create every loaded effect when effects are initialised`() {
        // Given
        val venom = decreaseHealthEffect(id = "venom").toDto()
        val teleport = teleportEffect(id = "teleport").toDto()
        whenever(effectLoader.loadEffects()).thenReturn(listOf(venom, teleport))
        // When
        initialiseEffects()
        // Then
        effectRepository.searchById("venom")?.toDto() shouldBe venom
        effectRepository.searchById("teleport")?.toDto() shouldBe teleport
        eventBus.shouldHavePublishedEvents(EffectCreated("venom"), EffectCreated("teleport"))
    }

    @Test
    fun `should not create an effect again when the effect already exists`() {
        // Given
        val venom = decreaseHealthEffect(id = "venom")
        effectRepository.create(venom)
        whenever(effectLoader.loadEffects()).thenReturn(listOf(decreaseHealthEffect(id = "venom", damage = 99).toDto()))
        // When
        initialiseEffects()
        // Then
        effectRepository.searchById("venom")?.toDto() shouldBe venom.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }
}
