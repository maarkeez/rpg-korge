package com.mkz.tessavar.effect.usecases.queries

import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.domain.EffectMother.effectId
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchEffectByIdTest {
    private val effectRepository = InMemoryEffectRepository()
    private val searchEffectById = SearchEffectById(effectRepository)

    @Test
    fun `should return effect when it exists`() {
        // Given
        val effect = damageEffect()
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
