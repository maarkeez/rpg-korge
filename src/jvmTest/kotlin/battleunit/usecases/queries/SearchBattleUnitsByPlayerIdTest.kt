package battleunit.usecases.queries

import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import player.domain.PlayerMother
import unit.domain.UnitMother

class SearchBattleUnitsByPlayerIdTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitsByPlayerId = SearchBattleUnitsByPlayerId(battleUnitRepository)

    @Test
    fun `should return battle units when the player has battle units`() {
        // Given
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
        val firstBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit(player = player.toDto())
        val secondBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother
                .battleUnit(player = player.toDto())
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
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
        val aliveBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother.battleUnit(
                player = player.toDto(),
                unit =
                    _root_ide_package_.unit.domain.UnitMother
                        .unit(healthPoints = 10)
                        .toDto(),
            )
        val defeatedBattleUnit =
            _root_ide_package_.battleunit.domain.BattleUnitMother.battleUnit(
                player = player.toDto(),
                unit =
                    _root_ide_package_.unit.domain.UnitMother
                        .unit(healthPoints = 0)
                        .toDto(),
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
