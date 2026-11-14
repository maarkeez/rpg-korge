package com.mkz.tessavar.battle.domain

import korlibs.io.util.UUID

object BattleMother {
    fun battle(
        players: List<String> = listOf(playerId(), playerId()),
        battleUnitIds: Set<String> = setOf("battle-unit-1", "battle-unit-2"),
        battleUnits: Map<String, String> = battleUnitsOfPlayersInTurns(battleUnitIds, players),
    ): Battle = Battle.startFirstRound(players, battleUnits).pullEvents().second

    fun finishedBattle(players: List<String> = listOf(playerId(), playerId())): Battle = battle(players).finish().pullEvents().second

    fun playerId() = "player-${UUID.randomUUID()}"

    private fun battleUnitsOfPlayersInTurns(
        battleUnitIds: Set<String>,
        players: List<String>,
    ): Map<String, String> = battleUnitIds.withIndex().associate { (index, battleUnitId) -> battleUnitId to players[index % players.size] }
}
