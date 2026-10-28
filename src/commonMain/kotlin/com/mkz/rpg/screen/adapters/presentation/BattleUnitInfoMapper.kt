package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities.AbilityAvailability

/** Maps a battle unit to what the views of the battle unit draw, for the presenters displaying them. */
internal object BattleUnitInfoMapper {
    fun battleUnitInfo(battleUnit: BattleUnit.Dto): BattleUnitInfo =
        BattleUnitInfo(
            unitId = battleUnit.unitId,
            remainingHealthPoints = battleUnit.remainingHealthPoints,
            maximumHealthPoints = battleUnit.maximumHealthPoints,
            remainingManaPoints = battleUnit.remainingManaPoints,
            maximumManaPoints = battleUnit.maximumManaPoints,
            effectIds = battleUnit.ongoingEffects.onTurnStarted + battleUnit.ongoingEffects.onDefeatedEffects,
        )

    fun turnActions(battleUnit: BattleUnit.Dto): BattleUnitInfoView.TurnActions =
        BattleUnitInfoView.TurnActions(
            remainingSteps = battleUnit.remainingTurnActions.remainingSteps,
            maximumSteps = battleUnit.remainingTurnActions.maximumSteps,
            remainingCasts = battleUnit.remainingTurnActions.remainingCasts,
            maximumCasts = battleUnit.remainingTurnActions.maximumCasts,
        )

    fun abilities(abilityAvailabilities: List<AbilityAvailability>): List<BattleUnitInfoView.Ability> =
        abilityAvailabilities.map { abilityAvailability ->
            BattleUnitInfoView.Ability(
                abilityId = abilityAvailability.abilityId,
                cooldownTurnsLeft = abilityAvailability.cooldownTurnsLeft,
                hasEnoughMana = abilityAvailability.hasEnoughMana,
            )
        }
}
