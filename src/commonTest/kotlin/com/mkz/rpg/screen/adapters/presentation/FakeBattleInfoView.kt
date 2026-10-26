package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA

// Records what the presenters ask the battle info view to display, instead of drawing it.
class FakeBattleInfoView : BattleInfoView(TextsMother.texts()) {
    data class BattleInfo(
        val playerId: String,
        val round: Int,
        val turnColor: RGBA,
    )

    val displayedBattleInfos = mutableListOf<BattleInfo>()
    val announcedTurns = mutableListOf<BattleInfo>()
    val displayedPlayerWins = mutableListOf<String>()
    var explorationDisplays = 0
    var announcedBattles = 0
    var announcedBattleEnds = 0

    override fun displayExploring() {
        explorationDisplays++
    }

    override suspend fun announceBattle() {
        announcedBattles++
    }

    override suspend fun announceBattleEnd() {
        announcedBattleEnds++
    }

    override fun displayBattleInfo(
        playerId: String,
        round: Int,
        turnColor: RGBA,
    ) {
        displayedBattleInfos += BattleInfo(playerId, round, turnColor)
    }

    override suspend fun announceTurn(
        playerId: String,
        round: Int,
        turnColor: RGBA,
    ) {
        announcedTurns += BattleInfo(playerId, round, turnColor)
    }

    override fun displayPlayerWin(playerId: String) {
        displayedPlayerWins += playerId
    }
}
