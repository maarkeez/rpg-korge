package com.mkz.rpg.screen.adapters.presentation

data class BattleUnitInfo(
    val unitId: String,
    val remainingHealth: Int,
    val maximumHealth: Int,
    val remainingMana: Int,
    val maximumMana: Int,
    val effectIds: List<String>,
    val isHumanPlayers: Boolean,
)
