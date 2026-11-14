package com.mkz.tessavar.effect.usecases.commands

import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.domain.EffectEvent.EffectCreated
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.domain.EffectMother.teleportEffect
import com.mkz.tessavar.effect.usecases.services.FakeEffectLoader
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseEffectsTest {
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()

    @Test
    fun `should create every loaded effect when effects are initialised`() {
        // Given
        val venom = damageEffect(id = "venom").toDto()
        val teleport = teleportEffect(id = "teleport").toDto()
        val initialiseEffects = InitialiseEffects(FakeEffectLoader(effects = listOf(venom, teleport)), effectRepository, eventBus)
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
        val venom = damageEffect(id = "venom")
        effectRepository.create(venom)
        val initialiseEffects = InitialiseEffects(FakeEffectLoader(effects = listOf(damageEffect(id = "venom", damage = 99).toDto())), effectRepository, eventBus)
        // When
        initialiseEffects()
        // Then
        effectRepository.searchById("venom")?.toDto() shouldBe venom.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }
}
