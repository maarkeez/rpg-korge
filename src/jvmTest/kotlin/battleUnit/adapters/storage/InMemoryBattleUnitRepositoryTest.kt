package battleUnit.adapters.storage

import battleUnit.domain.BattleUnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import player.domain.PlayerMother

class InMemoryBattleUnitRepositoryTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()

    @Nested
    inner class Create {
        @Test
        fun `should store the battle unit when the battle unit is created`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            // When
            battleUnitRepository.create(battleUnit)
            // Then
            val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)
            assertThat(storedBattleUnit).isEqualTo(battleUnit)
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should replace the stored battle unit when the battle unit is updated`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            battleUnitRepository.create(battleUnit)
            val updatedBattleUnit = battleUnit.resetActions()
            // When
            battleUnitRepository.update(updatedBattleUnit)
            // Then
            val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)
            assertThat(storedBattleUnit).isEqualTo(updatedBattleUnit)
        }
    }

    @Nested
    inner class SearchById {
        @Test
        fun `should return the battle unit when the battle unit is stored`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            battleUnitRepository.create(battleUnit)
            // When
            val storedBattleUnit = battleUnitRepository.searchById(battleUnit.toDto().id)
            // Then
            assertThat(storedBattleUnit).isEqualTo(battleUnit)
        }

        @Test
        fun `should return null when the battle unit is not stored`() {
            // When
            val storedBattleUnit = battleUnitRepository.searchById("battle-unit-unknown")
            // Then
            assertThat(storedBattleUnit).isNull()
        }
    }

    @Nested
    inner class SearchByPlayerId {
        @Test
        fun `should return the battle units of the player when other players have battle units stored`() {
            // Given
            val player = PlayerMother.player(id = "player-1").toDto()
            val otherPlayer = PlayerMother.player(id = "player-2").toDto()
            val playerBattleUnit = BattleUnitMother.battleUnit(player = player)
            val otherPlayerBattleUnit = BattleUnitMother.battleUnit(player = otherPlayer)
            battleUnitRepository.create(playerBattleUnit)
            battleUnitRepository.create(otherPlayerBattleUnit)
            // When
            val storedBattleUnits = battleUnitRepository.searchByPlayerId(playerId = player.id)
            // Then
            assertThat(storedBattleUnits).containsExactly(playerBattleUnit)
        }

        @Test
        fun `should return an empty list when the player has no battle units stored`() {
            // Given
            val otherPlayer = PlayerMother.player(id = "player-2").toDto()
            battleUnitRepository.create(BattleUnitMother.battleUnit(player = otherPlayer))
            // When
            val storedBattleUnits = battleUnitRepository.searchByPlayerId(playerId = "player-1")
            // Then
            assertThat(storedBattleUnits).isEmpty()
        }
    }

    @Nested
    inner class SearchAll {
        @Test
        fun `should return all the stored battle units when battle units are stored`() {
            // Given
            val firstBattleUnit = BattleUnitMother.battleUnit()
            val secondBattleUnit = BattleUnitMother.battleUnit()
            battleUnitRepository.create(firstBattleUnit)
            battleUnitRepository.create(secondBattleUnit)
            // When
            val storedBattleUnits = battleUnitRepository.searchAll()
            // Then
            assertThat(storedBattleUnits).containsExactlyInAnyOrder(firstBattleUnit, secondBattleUnit)
        }

        @Test
        fun `should return an empty list when no battle unit is stored`() {
            // When
            val storedBattleUnits = battleUnitRepository.searchAll()
            // Then
            assertThat(storedBattleUnits).isEmpty()
        }
    }
}
