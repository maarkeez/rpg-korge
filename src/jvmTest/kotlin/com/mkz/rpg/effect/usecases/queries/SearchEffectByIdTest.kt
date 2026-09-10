package com.mkz.rpg.effect.usecases.queries

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.effect
import com.mkz.rpg.effect.domain.EffectMother.effectId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchEffectByIdTest {
    private val effectRepository = InMemoryEffectRepository()
    private val searchEffectById = SearchEffectById(effectRepository)

    @Test
    fun `should return effect when it exists`() {
        // Given
        val effect = effect()
        val effectDto = effect.toDto()
        effectRepository.create(effect)
        // When
        val storedEffect = searchEffectById(effectDto.id)
        // Then
        assertThat(storedEffect).isEqualTo(effectDto)
    }

    @Test
    fun `should return null when effect does not exist`() {
        // Given
        val unknownEffectId = effectId()
        // When
        val storedEffect = searchEffectById(unknownEffectId)
        // Then
        assertThat(storedEffect).isNull()
    }
}
