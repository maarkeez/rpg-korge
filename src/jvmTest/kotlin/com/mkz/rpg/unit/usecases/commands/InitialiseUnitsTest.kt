package com.mkz.rpg.unit.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitEvent.UnitCreated
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.services.FakeUnitLoader
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseUnitsTest {
    private val unitRepository = InMemoryUnitRepository()
    private val eventBus = FakeEventBus()

    @Test
    fun `should create every loaded unit when units are initialised`() {
        // Given
        val knight = unit(id = "knight").toDto()
        val rat = unit(id = "rat").toDto()
        val initialiseUnits = InitialiseUnits(FakeUnitLoader(units = listOf(knight, rat)), unitRepository, eventBus)
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
        val initialiseUnits = InitialiseUnits(FakeUnitLoader(units = listOf(unit(id = "knight").toDto())), unitRepository, eventBus)
        // When
        initialiseUnits()
        // Then
        unitRepository.searchById("knight")?.toDto() shouldBe knight.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }
}
