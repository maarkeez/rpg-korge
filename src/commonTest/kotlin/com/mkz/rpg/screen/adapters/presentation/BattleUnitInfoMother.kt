package com.mkz.rpg.screen.adapters.presentation

object BattleUnitInfoMother {
    fun battleUnitInfo(
        unitId: String = "knight",
        remainingHealth: Int = 20,
        maximumHealth: Int = 20,
        remainingMana: Int = 10,
        maximumMana: Int = 10,
        effectIds: List<String> = emptyList(),
        isHumanPlayers: Boolean = true,
    ) = BattleUnitInfo(
        unitId = unitId,
        remainingHealth = remainingHealth,
        maximumHealth = maximumHealth,
        remainingMana = remainingMana,
        maximumMana = maximumMana,
        effectIds = effectIds,
        isHumanPlayers = isHumanPlayers,
    )

    fun turnActions(
        remainingSteps: Int = 3,
        maximumSteps: Int = 3,
        remainingCasts: Int = 1,
        maximumCasts: Int = 1,
    ) = BattleUnitInfoView.TurnActions(
        remainingSteps = remainingSteps,
        maximumSteps = maximumSteps,
        remainingCasts = remainingCasts,
        maximumCasts = maximumCasts,
    )
}
