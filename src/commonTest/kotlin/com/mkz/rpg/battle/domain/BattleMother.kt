package com.mkz.rpg.battle.domain

import korlibs.io.util.UUID

object BattleMother {
    fun battle(
        players: List<String> = listOf(playerId(), playerId()),
        battleUnitIds: Set<String> = setOf("battle-unit-1", "battle-unit-2"),
    ): Battle = Battle.startFirstRound(players, battleUnitIds).pullEvents().second

    fun finishedBattle(players: List<String> = listOf(playerId(), playerId())): Battle = battle(players).defeatPlayer(players.first()).pullEvents().second

    fun playerId() = "player-${UUID.randomUUID()}"
}
