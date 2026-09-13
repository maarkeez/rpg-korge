package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ResetBattleUnitActionsAndReduceCooldownsTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val resetBattleUnitActionsAndReduceCooldowns =
        ResetBattleUnitActionsAndReduceCooldowns(battleUnitRepository)

    @Test
    fun `should reset actions and reduce cooldowns for player battle units`() {
        // Given
        val unit = unit(movementRange = 4, abilities = listOf("ability-1")).toDto()
        val player = player(id = "player-1").toDto()
        val exhaustedBattleUnit =
            com.mkz.rpg.battleUnit.domain.BattleUnitMother
                .battleUnit(unit = unit, player = player)
                .move(distance = 2, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2)
                .castAbility(abilityId = "ability-1", abilityCooldown = 3, abilityCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        battleUnitRepository.create(exhaustedBattleUnit)
        // When
        resetBattleUnitActionsAndReduceCooldowns("player-1")
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(exhaustedBattleUnit.toDto().id)?.toDto()
        assertThat(storedBattleUnit!!.remainingTurnActions.remainingSteps).isEqualTo(4)
        assertThat(storedBattleUnit.remainingTurnActions.remainingCasts).isEqualTo(1)
        assertThat(storedBattleUnit.abilityCooldowns["ability-1"]).isEqualTo(2)
    }

    @Test
    fun `should not update any battle unit when the player has no battle units`() {
        // Given
        // When
        resetBattleUnitActionsAndReduceCooldowns("player-1")
        // Then
        assertThat(battleUnitRepository.searchAll()).isEmpty()
    }
}
