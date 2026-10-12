package com.mkz.rpg.unit.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitEvent.UnitCreated
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.services.UnitLoader
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseUnitsTest {
    private val unitRepository = InMemoryUnitRepository()
    private val eventBus = FakeEventBus()
    private val unitLoader: UnitLoader = mock()
    private val initialiseUnits = InitialiseUnits(unitLoader, unitRepository, eventBus)

    @Test
    fun `should create every loaded unit when units are initialised`() {
        // Given
        val knight = unit(id = "knight").toDto()
        val rat = unit(id = "rat").toDto()
        whenever(unitLoader.loadUnits()).thenReturn(listOf(knight, rat))
        // When
        initialiseUnits()
        // Then
        unitRepository.searchById("knight")?.toDto() shouldBe knight
        unitRepository.searchById("rat")?.toDto() shouldBe rat
        eventBus.shouldHavePublishedEvents(UnitCreated("knight"), UnitCreated("rat"))
    }

    @Test
    fun `should not create a unit again when the unit already exists`() {
        // Given
        val knight = unit(id = "knight")
        unitRepository.create(knight)
        whenever(unitLoader.loadUnits()).thenReturn(listOf(unit(id = "knight").toDto()))
        // When
        initialiseUnits()
        // Then
        unitRepository.searchById("knight")?.toDto() shouldBe knight.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }
}
