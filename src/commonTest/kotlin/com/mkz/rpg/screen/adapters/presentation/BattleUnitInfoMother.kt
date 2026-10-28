package com.mkz.rpg.screen.adapters.presentation

object BattleUnitInfoMother {
    fun battleUnitInfo(
        unitId: String = "knight",
        remainingHealthPoints: Int = 20,
        maximumHealthPoints: Int = 20,
        remainingManaPoints: Int = 10,
        maximumManaPoints: Int = 10,
        effectIds: List<String> = emptyList(),
    ) = BattleUnitInfo(
        unitId = unitId,
        remainingHealthPoints = remainingHealthPoints,
        maximumHealthPoints = maximumHealthPoints,
        remainingManaPoints = remainingManaPoints,
        maximumManaPoints = maximumManaPoints,
        effectIds = effectIds,
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
