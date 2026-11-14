package com.mkz.tessavar.unit.usecases.commands

import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.domain.UnitEvent
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RequestUnitCreationTest {
    private val unitRepository = InMemoryUnitRepository()
    private val eventBus = FakeEventBus()
    private val requestUnitCreation =
        RequestUnitCreation(
            unitRepository = unitRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create unit`() {
        // Given
        val unit = unit().toDto()
        // When
        requestUnitCreation(unitDto = unit)
        // Then
        val storedUnit = unitRepository.searchById(unit.id)?.toDto()
        storedUnit shouldBe unit
        eventBus.shouldHavePublishedEvents(UnitEvent.UnitCreated(unit.id))
    }
}
