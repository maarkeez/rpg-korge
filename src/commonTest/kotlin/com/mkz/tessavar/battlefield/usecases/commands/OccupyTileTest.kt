package com.mkz.tessavar.battlefield.usecases.commands

import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldError
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class OccupyTileTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val occupyTile =
        OccupyTile(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should occupy tile when the tile is vacant`() {
        // Given
        battlefieldRepository.create(battlefield())
        val occupantId = "battle-unit-1"
        // When
        occupyTile(row = 1, column = 1, occupantId = occupantId)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        storedBattlefield?.tiles?.get(Battlefield.Dto.PositionDto(1, 1))?.occupantId shouldBe occupantId
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.TileOccupied(1, 1, occupantId),
        )
    }

    @Test
    fun `should throw tile error when the tile is occupied`() {
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
                occupyTile(row = 1, column = 1, occupantId = "battle-unit-1")
            }
        // Then
        error.shouldBeInstanceOf<BattlefieldError.TileIsOccupied>()
    }

    @Test
    fun `should not occupy tile when no battlefield exists`() {
        // Given
        // When
        occupyTile(row = 1, column = 1, occupantId = "battle-unit-1")
        // Then
        battlefieldRepository.search().shouldBeNull()
        eventBus.publishedEvents.shouldBeEmpty()
    }
}
