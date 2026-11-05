package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

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
                .walkInBattle(fromRow = 0, fromColumn = 0, path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2)))
                .castAbility(abilityId = "ability-1", abilityCooldown = 3, abilityManaCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        battleUnitRepository.create(exhaustedBattleUnit)
        // When
        resetBattleUnitActionsAndReduceCooldowns("player-1", setOf(exhaustedBattleUnit.toDto().id))
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(exhaustedBattleUnit.toDto().id)?.toDto()
        storedBattleUnit!!.remainingActions.remainingSteps shouldBe 4
        storedBattleUnit.remainingActions.remainingCasts shouldBe 1
        storedBattleUnit.knownAbilities["ability-1"] shouldBe 2
    }

    @Test
    fun `should not reset actions nor reduce cooldowns when the battle unit is not in the battle`() {
        // Given
        val unit = unit(movementRange = 4, abilities = listOf("ability-1")).toDto()
        val player = player(id = "player-1").toDto()
        val exhaustedBattleUnit =
            com.mkz.rpg.battleUnit.domain.BattleUnitMother
                .battleUnit(unit = unit, player = player)
                .walkInBattle(fromRow = 0, fromColumn = 0, path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2)))
                .castAbility(abilityId = "ability-1", abilityCooldown = 3, abilityManaCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        battleUnitRepository.create(exhaustedBattleUnit)
        // When
        resetBattleUnitActionsAndReduceCooldowns("player-1", setOf("another-battle-unit"))
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(exhaustedBattleUnit.toDto().id)?.toDto()
        storedBattleUnit!!.remainingActions.remainingSteps shouldBe 2
        storedBattleUnit.remainingActions.remainingCasts shouldBe 0
        storedBattleUnit.knownAbilities["ability-1"] shouldBe 3
    }

    @Test
    fun `should not update any battle unit when the player has no battle units`() {
        // Given
        // When
        resetBattleUnitActionsAndReduceCooldowns("player-1", emptySet())
        // Then
        battleUnitRepository.searchAll().shouldBeEmpty()
    }
}
