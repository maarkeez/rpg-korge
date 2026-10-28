package com.mkz.rpg.screen.adapters.presentation

/**
 * What the battle unit views draw of a battle unit: the portrait and the name of its unit, its health and its mana, and
 * the effects it is under. Mapped from the domain by the presenters, see [BattleUnitInfoMapper].
 */
data class BattleUnitInfo(
    val unitId: String,
    val remainingHealthPoints: Int,
    val maximumHealthPoints: Int,
    val remainingManaPoints: Int,
    val maximumManaPoints: Int,
    // The effects applied when each turn starts, followed by the ones applied when it is defeated.
    val effectIds: List<String>,
)
