package battleunit.usecases.queries

import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import player.domain.PlayerMother
import unit.domain.UnitMother

class HasAllBattleUnitsDefeatedTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)

    @Test
    fun `should return true when all player battle units are defeated`() {
        // Given
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
        repeat(2) {
            battleUnitRepository.create(
                _root_ide_package_.battleunit.domain.BattleUnitMother.battleUnit(
                    player = player.toDto(),
                    unit =
                        _root_ide_package_.unit.domain.UnitMother
                            .unit(healthPoints = 0)
                            .toDto(),
                ),
            )
        }
        // When
        val result = hasAllBattleUnitsDefeated("player-1")
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when at least one battle unit is alive`() {
        // Given
        val player =
            _root_ide_package_.player.domain.PlayerMother
                .player(id = "player-1")
        battleUnitRepository.create(
            _root_ide_package_.battleunit.domain.BattleUnitMother.battleUnit(
                player = player.toDto(),
                unit =
                    _root_ide_package_.unit.domain.UnitMother
                        .unit(healthPoints = 0)
                        .toDto(),
            ),
        )
        battleUnitRepository.create(
            _root_ide_package_.battleunit.domain.BattleUnitMother.battleUnit(
                player = player.toDto(),
                unit =
                    _root_ide_package_.unit.domain.UnitMother
                        .unit(healthPoints = 10)
                        .toDto(),
            ),
        )
        // When
        val result = hasAllBattleUnitsDefeated("player-1")
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when the player has no battle units`() {
        // Given
        // When
        val result = hasAllBattleUnitsDefeated("player-1")
        // Then
        assertThat(result).isTrue()
    }
}
