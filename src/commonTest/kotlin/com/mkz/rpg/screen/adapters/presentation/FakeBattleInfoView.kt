package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA

// Records what the presenters ask the battle info view to display, instead of drawing it.
class FakeBattleInfoView : BattleInfoView() {
    data class BattleInfo(
        val playerName: String,
        val round: Int,
        val turnColor: RGBA,
    )

    val displayedBattleInfos = mutableListOf<BattleInfo>()
    val announcedTurns = mutableListOf<BattleInfo>()
    val displayedPlayerWins = mutableListOf<String>()

    override fun displayBattleInfo(
        playerName: String,
        round: Int,
        turnColor: RGBA,
    ) {
        displayedBattleInfos += BattleInfo(playerName, round, turnColor)
    }

    override suspend fun announceTurn(
        playerName: String,
        round: Int,
        turnColor: RGBA,
    ) {
        announcedTurns += BattleInfo(playerName, round, turnColor)
    }

    override fun displayPlayerWin(playerName: String) {
        displayedPlayerWins += playerName
    }
}
