package com.mkz.rpg.effect.adapters.storage

import com.mkz.rpg.effect.domain.EffectMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryEffectRepositoryTest {
    private val effectRepository = InMemoryEffectRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the effect when the effect is created`() {
            // Given
            val effect = EffectMother.effect()
            // When
            effectRepository.create(effect)
            // Then
            val storedEffect = effectRepository.searchById(effect.toDto().id)
            assertThat(storedEffect).isEqualTo(effect)
        }
    }

    @Nested
    inner class SearchById {
        @Test
        fun `should return the effect when the effect is stored`() {
            // Given
            val effect = EffectMother.effect()
            effectRepository.create(effect)
            // When
            val storedEffect = effectRepository.searchById(effect.toDto().id)
            // Then
            assertThat(storedEffect).isEqualTo(effect)
        }

        @Test
        fun `should return null when the effect is not stored`() {
            // When
            val storedEffect = effectRepository.searchById("effect-unknown")
            // Then
            assertThat(storedEffect).isNull()
        }
    }
}
