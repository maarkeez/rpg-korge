package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasAllBattleUnitsDefeatedTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val hasAllBattleUnitsDefeated = HasAllBattleUnitsDefeated(battleUnitRepository)

    @Test
    fun `should return true when all player battle units are defeated`() {
        // Given
        val player =
            player(id = "player-1")
        repeat(2) {
            battleUnitRepository.create(
                battleUnit(
                    player = player.toDto(),
                    unit =
                        unit(healthPoints = 0)
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
        val player = player(id = "player-1")
        battleUnitRepository.create(
            battleUnit(
                player = player.toDto(),
                unit = unit(healthPoints = 0).toDto(),
            ),
        )
        battleUnitRepository.create(
            battleUnit(
                player = player.toDto(),
                unit = unit(healthPoints = 10).toDto(),
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
