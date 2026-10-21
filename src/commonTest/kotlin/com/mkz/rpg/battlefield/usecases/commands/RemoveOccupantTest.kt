package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class RemoveOccupantTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val removeOccupant =
        RemoveOccupant(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should remove occupant when the battle unit is deployed`() {
        // Given
        val occupantId = "battle-unit-1"
        val battlefield =
            battlefield()
                .occupy(1, 1, occupantId)
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        removeOccupant(occupantId)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        storedBattlefield
            ?.tiles
            ?.get(Battlefield.Dto.PositionDto(1, 1))
            ?.occupantId
            .shouldBeNull()
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.OccupantRemoved(occupantId, 1, 1),
        )
    }

    @Test
    fun `should not remove occupant when the battle unit is not deployed`() {
        // Given
        battlefieldRepository.create(battlefield())
        // When
        removeOccupant("unknown-battle-unit")
        // Then
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
