package battleunit.usecases.queries

import battleunit.adapters.storage.*
import battleunit.domain.*
import org.junit.*
import player.domain.PlayerMother
import unit.domain.UnitMother

class HasAllBattleUnitsDefeatedTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)

    @Test
    fun `should return true when all player battle units are defeated`() {
        // Given
        val player = PlayerMother.player(id = "player-1")
        repeat(2) {
            battleUnitRepository.create(
                BattleUnitMother.battleUnit(
                    player = player.toDto(),
                    unit = UnitMother.unit(healthPoints = 0).toDto(),
                )
            )
        }
        // When
        val result = hasAllBattleUnitsDefeated("player-1")
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isTrue()
    }

    @Test
    fun `should return false when at least one battle unit is alive`() {
        // Given
        val player = PlayerMother.player(id = "player-1")
        battleUnitRepository.create(
            BattleUnitMother.battleUnit(
                player = player.toDto(),
                unit = UnitMother.unit(healthPoints = 0).toDto(),
            )
        )
        battleUnitRepository.create(
            BattleUnitMother.battleUnit(
                player = player.toDto(),
                unit = UnitMother.unit(healthPoints = 10).toDto(),
            )
        )
        // When
        val result = hasAllBattleUnitsDefeated("player-1")
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isFalse()
    }

    @Test
    fun `should return true when the player has no battle units`() {
        // Given
        // When
        val result = hasAllBattleUnitsDefeated("player-1")
        // Then
        org.assertj.core.api.Assertions.assertThat(result).isTrue()
    }
}
