package com.mkz.rpg.battle.domain

import kotlin.random.Random

object BattleMother {
    fun battle(players: List<String> = listOf(playerId(), playerId())): Battle = Battle.startFirstRound(players).pullEvents().second

    fun finishedBattle(players: List<String> = listOf(playerId(), playerId())): Battle = battle(players).defeatPlayer(players.first()).pullEvents().second

    fun playerId() = "player-${Random.nextInt(1, 100)}"
}
