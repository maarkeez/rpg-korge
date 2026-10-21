package com.mkz.rpg.battle.domain

import korlibs.io.util.UUID

object BattleMother {
    fun battle(
        players: List<String> = listOf(playerId(), playerId()),
        battleUnitIds: Set<String> = setOf("battle-unit-1", "battle-unit-2"),
        // Each battle unit belongs to a player, in turns: the first one to the first player, the second one to the second player...
        battleUnits: Map<String, String> = battleUnitIds.withIndex().associate { (index, battleUnitId) -> battleUnitId to players[index % players.size] },
    ): Battle = Battle.startFirstRound(players, battleUnits).pullEvents().second

    fun finishedBattle(players: List<String> = listOf(playerId(), playerId())): Battle = battle(players).defeatPlayer(players.first()).pullEvents().second

    fun playerId() = "player-${UUID.randomUUID()}"
}
