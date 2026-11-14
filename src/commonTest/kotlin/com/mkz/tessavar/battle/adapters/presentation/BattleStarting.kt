package com.mkz.tessavar.battle.adapters.presentation

import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi

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
