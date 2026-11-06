package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities.AbilityAvailability

/** Maps a battle unit to what the views of the battle unit draw, for the presenters displaying them. */
internal object BattleUnitInfoMapper {
    fun battleUnitInfo(
        battleUnit: BattleUnit.Dto,
        isAlly: Boolean,
    ): BattleUnitInfo =
        BattleUnitInfo(
            unitId = battleUnit.unitId,
            remainingHealthPoints = battleUnit.remainingHealth,
            maximumHealthPoints = battleUnit.maximumHealth,
            remainingManaPoints = battleUnit.remainingMana,
            maximumManaPoints = battleUnit.maximumMana,
            effectIds = battleUnit.ongoingEffects.onPlayerTurnStarted + battleUnit.ongoingEffects.onDefeatedEffects,
            isAlly = isAlly,
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
                cooldownTurnsLeft = abilityAvailability.remainingCooldown,
                hasEnoughMana = abilityAvailability.hasEnoughMana,
                manaCost = abilityAvailability.manaCost,
            )
        }
}
