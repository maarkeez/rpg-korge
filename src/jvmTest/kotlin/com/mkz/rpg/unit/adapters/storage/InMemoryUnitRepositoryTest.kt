package com.mkz.rpg.unit.adapters.storage

import com.mkz.rpg.unit.domain.UnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryUnitRepositoryTest {
    private val unitRepository = InMemoryUnitRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the unit when the unit is created`() {
            // Given
            val unit = UnitMother.unit()
            // When
            unitRepository.create(unit)
            // Then
            val storedUnit = unitRepository.searchById(unit.toDto().id)
            assertThat(storedUnit).isEqualTo(unit)
        }
    }

    @Nested
    inner class SearchById {
        @Test
        fun `should return the unit when the unit is stored`() {
            // Given
            val unit = UnitMother.unit()
            unitRepository.create(unit)
            // When
            val storedUnit = unitRepository.searchById(unit.toDto().id)
            // Then
            assertThat(storedUnit).isEqualTo(unit)
        }

        @Test
        fun `should return null when the unit is not stored`() {
            // When
            val storedUnit = unitRepository.searchById("unit-unknown")
            // Then
            assertThat(storedUnit).isNull()
        }
    }
}
