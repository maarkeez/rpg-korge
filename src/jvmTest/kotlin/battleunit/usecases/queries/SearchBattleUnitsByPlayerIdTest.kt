package battleunit.usecases.queries

import battleunit.adapters.storage.*
import battleunit.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import player.domain.*
import unit.domain.*

class SearchBattleUnitsByPlayerIdTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)

    @Test
    fun `should return battle units when the player has battle units`() {
        // Given
        val player = PlayerMother.player(id = "player-1")
        val firstBattleUnit = BattleUnitMother.battleUnit(player = player.toDto())
        val secondBattleUnit = BattleUnitMother.battleUnit(player = player.toDto())
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
        val player = PlayerMother.player(id = "player-1")
        val aliveBattleUnit = BattleUnitMother.battleUnit(
            player = player.toDto(),
            unit = UnitMother.unit(healthPoints = 10).toDto(),
        )
        val defeatedBattleUnit = BattleUnitMother.battleUnit(
            player = player.toDto(),
            unit = UnitMother.unit(healthPoints = 0).toDto(),
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
