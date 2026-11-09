package com.mkz.rpg.screen.adapters.presentation

/**
 * What the battle unit views draw of a battle unit: the portrait and the name of its unit, its health and its mana, and
 * the effects it is under, and whether it is the human player's. Mapped from the domain by the presenters, see [BattleUnitInfoMapper].
 */
data class BattleUnitInfo(
    val unitId: String,
    val remainingHealth: Int,
    val maximumHealth: Int,
    val remainingMana: Int,
    val maximumMana: Int,
    // The effects applied when each turn starts, followed by the ones applied when it is defeated.
    val effectIds: List<String>,
    // Whether it is the human player's, its portrait ringed green, or the CPU's, ringed red.
    val isHumanPlayers: Boolean,
)
