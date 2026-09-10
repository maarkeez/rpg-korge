package com.mkz.rpg.battlefield.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BattlefieldTest {
    @Nested
    inner class Create {
        @Test
        fun `should create a battlefield when the tile matrix matches the rows and columns`() {
            // Given
            val rows = 2
            val columns = 3
            val tiles = List(rows) { List(columns) { BattlefieldMother.terrainId() } }
            // When
            val createdBattlefield = Battlefield.create(rows, columns, tiles)
            // Then
            val battlefieldDto = createdBattlefield.toDto()
            assertThat(battlefieldDto.rows).isEqualTo(rows)
            assertThat(battlefieldDto.columns).isEqualTo(columns)
            assertThat(battlefieldDto.tiles).hasSize(rows * columns)
        }

        @Test
        fun `should publish the battlefield created event when the battlefield is created`() {
            // Given
            val tiles = List(3) { List(3) { BattlefieldMother.terrainId() } }
            // When
            val createdBattlefield = Battlefield.create(3, 3, tiles)
            // Then
            val (events, _) = createdBattlefield.pullEvents()
            assertThat(events).containsExactly(BattlefieldEvent.BattlefieldCreated)
        }
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the rows, columns and tiles when converted to dto`() {
            // Given
            val battlefield = BattlefieldMother.battlefield(rows = 2, columns = 3)
            // When
            val result = battlefield.toDto()
            // Then
            assertThat(result.rows).isEqualTo(2)
            assertThat(result.columns).isEqualTo(3)
            assertThat(result.tiles).hasSize(6)
        }
    }

    @Nested
    inner class Occupy {
        @Test
        fun `should occupy the tile and publish the event when the tile is vacant`() {
            // Given
            val battlefield = BattlefieldMother.battlefield()
            val battleUnitId = "battle-unit-1"
            // When
            val occupiedBattlefield = battlefield.occupy(row = 0, column = 0, battleUnitId = battleUnitId)
            // Then
            assertThat(occupiedBattlefield.occupant(row = 0, column = 0)).isEqualTo(battleUnitId)
            val (events, _) = occupiedBattlefield.pullEvents()
            assertThat(events)
                .containsExactly(BattlefieldEvent.BattlefieldTileOccupied(row = 0, column = 0, battlefieldUnitId = battleUnitId))
        }

        @Test
        fun `should free the previous tile when the battle unit occupies a new tile`() {
            // Given
            val battleUnitId = "battle-unit-1"
            val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, battleUnitId = battleUnitId)
            // When
            val occupiedBattlefield = battlefield.occupy(row = 0, column = 1, battleUnitId = battleUnitId)
            // Then
            assertThat(occupiedBattlefield.occupant(row = 0, column = 0)).isNull()
            assertThat(occupiedBattlefield.occupant(row = 0, column = 1)).isEqualTo(battleUnitId)
        }

        @Test
        fun `should fail when the tile is not vacant`() {
            // Given
            val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, battleUnitId = "battle-unit-1")
            // When
            val result = runCatching { battlefield.occupy(row = 0, column = 0, battleUnitId = "battle-unit-2") }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(BattlefieldError.TileIsNotVacant::class.java)
        }

        @Test
        fun `should fail when the tile is not found`() {
            // Given
            val battlefield = BattlefieldMother.battlefield()
            // When
            val result = runCatching { battlefield.occupy(row = 5, column = 5, battleUnitId = "battle-unit-1") }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(BattlefieldError.TileNotFound::class.java)
        }
    }

    @Nested
    inner class RemoveOccupant {
        @Test
        fun `should remove the occupant and publish the event when the battle unit is deployed`() {
            // Given
            val battleUnitId = "battle-unit-1"
            val battlefield =
                BattlefieldMother
                    .battlefield()
                    .occupy(row = 1, column = 1, battleUnitId = battleUnitId)
                    .pullEvents()
                    .second
            // When
            val updatedBattlefield = battlefield.removeOccupant(battleUnitId = battleUnitId)
            // Then
            assertThat(updatedBattlefield.occupant(row = 1, column = 1)).isNull()
            val (events, _) = updatedBattlefield.pullEvents()
            assertThat(events)
                .containsExactly(BattlefieldEvent.OccupantRemoved(battleUnitId = battleUnitId, row = 1, column = 1))
        }

        @Test
        fun `should not update the battlefield when the battle unit is not deployed`() {
            // Given
            val battlefield = BattlefieldMother.battlefield()
            // When
            val updatedBattlefield = battlefield.removeOccupant(battleUnitId = "battle-unit-1")
            // Then
            assertThat(updatedBattlefield).isEqualTo(battlefield)
        }
    }

    @Nested
    inner class CanBeOccupied {
        @Test
        fun `should be true when the tile is vacant`() {
            // Given
            val battlefield = BattlefieldMother.battlefield()
            // When
            val result = battlefield.canBeOccupied(row = 0, column = 0)
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should be false when the tile is occupied`() {
            // Given
            val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, battleUnitId = "battle-unit-1")
            // When
            val result = battlefield.canBeOccupied(row = 0, column = 0)
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class Occupant {
        @Test
        fun `should return the battle unit id when the tile is occupied`() {
            // Given
            val battleUnitId = "battle-unit-1"
            val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, battleUnitId = battleUnitId)
            // When
            val result = battlefield.occupant(row = 0, column = 0)
            // Then
            assertThat(result).isEqualTo(battleUnitId)
        }

        @Test
        fun `should return null when the tile is vacant`() {
            // Given
            val battlefield = BattlefieldMother.battlefield()
            // When
            val result = battlefield.occupant(row = 0, column = 0)
            // Then
            assertThat(result).isNull()
        }
    }

    @Nested
    inner class Position {
        @Test
        fun `should return the position when the battle unit is deployed`() {
            // Given
            val battleUnitId = "battle-unit-1"
            val battlefield = BattlefieldMother.battlefield().occupy(row = 1, column = 2, battleUnitId = battleUnitId)
            // When
            val result = battlefield.position(battleUnitId = battleUnitId)
            // Then
            assertThat(result).isEqualTo(Battlefield.Dto.PositionDto(row = 1, column = 2))
        }

        @Test
        fun `should return null when the battle unit is not deployed`() {
            // Given
            val battlefield = BattlefieldMother.battlefield()
            // When
            val result = battlefield.position(battleUnitId = "battle-unit-1")
            // Then
            assertThat(result).isNull()
        }
    }

    @Nested
    inner class IsInBoundaries {
        @Test
        fun `should be true when the position is within the battlefield`() {
            // Given
            val battlefield = BattlefieldMother.battlefield(rows = 3, columns = 3)
            // When
            val result = battlefield.isInBoundaries(row = 1, column = 1)
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should be false when the row is outside the battlefield`() {
            // Given
            val battlefield = BattlefieldMother.battlefield(rows = 3, columns = 3)
            // When
            val result = battlefield.isInBoundaries(row = 3, column = 0)
            // Then
            assertThat(result).isFalse
        }

        @Test
        fun `should be false when the column is outside the battlefield`() {
            // Given
            val battlefield = BattlefieldMother.battlefield(rows = 3, columns = 3)
            // When
            val result = battlefield.isInBoundaries(row = 0, column = 3)
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the pending events and clear them when the battlefield has events`() {
            // Given
            val battlefield = BattlefieldMother.battlefield().occupy(row = 0, column = 0, battleUnitId = "battle-unit-1")
            // When
            val (events, clearedBattlefield) = battlefield.pullEvents()
            // Then
            assertThat(events)
                .containsExactly(BattlefieldEvent.BattlefieldTileOccupied(row = 0, column = 0, battlefieldUnitId = "battle-unit-1"))
            assertThat(clearedBattlefield.pullEvents().first).isEmpty()
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val battlefield = BattlefieldMother.battlefield()
            // When
            val (events, _) = battlefield.pullEvents()
            // Then
            assertThat(events).isEmpty()
        }
    }
}
