package com.mkz.rpg.unit.usecases.queries

import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitMother
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchUnitByIdTest {
    private val unitRepository = InMemoryUnitRepository()
    private val searchUnitById = SearchUnitById(unitRepository = unitRepository)

    @Test
    fun `should find unit when exists`() {
        // Given
        val unit = unit()
        unitRepository.create(unit)
        // When
        val unitFound = searchUnitById(id = unit.toDto().id)
        // Then
        assertThat(unitFound).isEqualTo(unit.toDto())
    }

    @Test
    fun `should not find unit when it does not exist`() {
        // Given
        val unitId = UnitMother.id()
        // When
        val unitFound = searchUnitById(id = unitId)
        // Then
        assertThat(unitFound).isNull()
    }
}
