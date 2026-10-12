package com.mkz.rpg.effect.usecases.queries

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.effectId
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchEffectByIdTest {
    private val effectRepository = InMemoryEffectRepository()
    private val searchEffectById = SearchEffectById(effectRepository)

    @Test
    fun `should return effect when it exists`() {
        // Given
        val effect = decreaseHealthEffect()
        val effectDto = effect.toDto()
        effectRepository.create(effect)
        // When
        val storedEffect = searchEffectById(effectDto.id)
        // Then
        storedEffect shouldBe effectDto
    }

    @Test
    fun `should return null when effect does not exist`() {
        // Given
        val unknownEffectId = effectId()
        // When
        val storedEffect = searchEffectById(unknownEffectId)
        // Then
        storedEffect.shouldBeNull()
    }
}
