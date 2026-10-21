package com.mkz.rpg.battle.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi

/**
 * Starts the battle as if the first player engaged the second one with all its battle units: the first player plays
 * first, and every battle unit of both players is in the battle. For the tests that are not about engaging.
 */
fun BattleApi.startBattleWithEveryBattleUnit(
    battleUnitApi: BattleUnitApi,
    firstPlayerId: String,
    secondPlayerId: String,
) = startBattle(
    engagingPlayerId = firstPlayerId,
    engagedPlayerId = secondPlayerId,
    engagingBattleUnitIds = battleUnitApi.searchBattleUnitsByPlayerId(firstPlayerId).map { battleUnit -> battleUnit.id },
    engagedBattleUnitIds = battleUnitApi.searchBattleUnitsByPlayerId(secondPlayerId).map { battleUnit -> battleUnit.id },
)
