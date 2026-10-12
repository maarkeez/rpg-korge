package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldError
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class UpdateBattlefieldOccupancyTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val updateBattlefieldOccupancy =
        UpdateBattlefieldOccupancy(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should occupy tile when the tile is vacant`() {
        // Given
        battlefieldRepository.create(battlefield())
        val battleUnitId = "battle-unit-1"
        // When
        updateBattlefieldOccupancy(row = 1, column = 1, battleUnitId = battleUnitId)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        storedBattlefield?.tiles?.get(Battlefield.Dto.PositionDto(1, 1))?.battleUnitId shouldBe battleUnitId
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.BattlefieldTileOccupied(1, 1, battleUnitId),
        )
    }

    @Test
    fun `should throw tile error when the tile is not vacant`() {
        // Given
        val battlefield =
            battlefield()
                .occupy(1, 1, "other-battle-unit")
                .pullEvents()
                .second
        battlefieldRepository.create(battlefield)
        // When
        val error =
            shouldThrowAny {
                updateBattlefieldOccupancy(row = 1, column = 1, battleUnitId = "battle-unit-1")
            }
        // Then
        error.shouldBeInstanceOf<BattlefieldError.TileIsNotVacant>()
    }

    @Test
    fun `should not occupy tile when no battlefield exists`() {
        // Given
        // When
        updateBattlefieldOccupancy(row = 1, column = 1, battleUnitId = "battle-unit-1")
        // Then
        battlefieldRepository.search().shouldBeNull()
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
