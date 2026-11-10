package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities.AbilityAvailability

internal object BattleUnitInfoMapper {
    fun battleUnitInfo(
        battleUnit: BattleUnit.Dto,
        isHumanPlayers: Boolean,
    ): BattleUnitInfo =
        BattleUnitInfo(
            unitId = battleUnit.unitId,
            remainingHealth = battleUnit.remainingHealth,
            maximumHealth = battleUnit.maximumHealth,
            remainingMana = battleUnit.remainingMana,
            maximumMana = battleUnit.maximumMana,
            effectIds = battleUnit.ongoingEffects.onPlayerTurnStarted + battleUnit.ongoingEffects.onDefeatedEffects,
            isHumanPlayers = isHumanPlayers,
        )

    fun turnActions(battleUnit: BattleUnit.Dto): BattleUnitInfoView.TurnActions =
        BattleUnitInfoView.TurnActions(
            remainingSteps = battleUnit.remainingActions.remainingSteps,
            maximumSteps = battleUnit.remainingActions.maximumSteps,
            remainingCasts = battleUnit.remainingActions.remainingCasts,
            maximumCasts = battleUnit.remainingActions.maximumCasts,
        )

    fun abilities(abilityAvailabilities: List<AbilityAvailability>): List<BattleUnitInfoView.Ability> =
        abilityAvailabilities.map { abilityAvailability ->
            BattleUnitInfoView.Ability(
                abilityId = abilityAvailability.abilityId,
                remainingCooldown = abilityAvailability.remainingCooldown,
                hasEnoughMana = abilityAvailability.hasEnoughMana,
                manaCost = abilityAvailability.manaCost,
            )
        }
}
