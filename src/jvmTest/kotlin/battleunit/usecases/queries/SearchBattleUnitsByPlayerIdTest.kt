package battleunit.usecases.queries

import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother.battleUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import player.domain.PlayerMother.player
import unit.domain.UnitMother.unit

class SearchBattleUnitsByPlayerIdTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)

    @Test
    fun `should return battle units when the player has battle units`() {
        // Given
        val player =
            player(id = "player-1")
        val firstBattleUnit =
            battleUnit(player = player.toDto())
        val secondBattleUnit =
            battleUnit(player = player.toDto())
        battleUnitRepository.create(firstBattleUnit)
        battleUnitRepository.create(secondBattleUnit)
        // When
        val result = searchBattleUnitsByPlayerId("player-1")
        // Then
        assertThat(result)
            .containsExactlyInAnyOrder(firstBattleUnit.toDto(), secondBattleUnit.toDto())
    }

    @Test
    fun `should not return defeated battle units when the player has defeated battle units`() {
        // Given
        val player = player(id = "player-1")
        val aliveBattleUnit =
            battleUnit(
                player = player.toDto(),
                unit = unit(healthPoints = 10).toDto(),
            )
        val defeatedBattleUnit =
            battleUnit(
                player = player.toDto(),
                unit = unit(healthPoints = 0).toDto(),
            )
        battleUnitRepository.create(aliveBattleUnit)
        battleUnitRepository.create(defeatedBattleUnit)
        // When
        val result = searchBattleUnitsByPlayerId("player-1")
        // Then
        assertThat(result).containsExactly(aliveBattleUnit.toDto())
    }

    @Test
    fun `should return empty list when the player has no battle units`() {
        // Given
        // When
        val result = searchBattleUnitsByPlayerId("player-1")
        // Then
        assertThat(result).isEmpty()
    }
}
