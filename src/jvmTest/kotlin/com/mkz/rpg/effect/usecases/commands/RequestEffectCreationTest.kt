package com.mkz.rpg.effect.usecases.commands

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RequestEffectCreationTest {
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()
    private val requestEffectCreation =
        RequestEffectCreation(
            effectRepository = effectRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create effect when it does not exist`() {
        // Given
        val effect = decreaseHealthEffect().toDto()
        // When
        requestEffectCreation(effectDto = effect)
        // Then
        val storedEffect = effectRepository.searchById(effect.id)?.toDto()
        storedEffect shouldBe effect
        eventBus.shouldHavePublishedEvents(EffectEvent.EffectCreated(effect.id))
    }

    @Test
    fun `should not create effect when it already exists`() {
        // Given
        val existingEffect = decreaseHealthEffect(damage = 3)
        val duplicateEffect = decreaseHealthEffect(existingEffect.toDto().id, damage = 10)
        effectRepository.create(existingEffect)
        // When
        requestEffectCreation(effectDto = duplicateEffect.toDto())
        // Then
        val storedEffect = effectRepository.searchById(existingEffect.toDto().id)?.toDto()
        storedEffect shouldBe existingEffect.toDto()
        eventBus.shouldHavePublishedEvents()
    }
}
