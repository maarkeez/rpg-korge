package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.RGBA

// Records what the presenters ask the banner view to display, instead of drawing it.
class FakeBannerView : BannerView(MessagesMother.messages()) {
    data class Banner(
        val playerId: String,
        val round: Int,
        val turnColor: RGBA,
    )

    val displayedBanners = mutableListOf<Banner>()
    val announcedTurns = mutableListOf<Banner>()
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

    override suspend fun announceBattleFinished() {
        announcedBattleEnds++
    }

    override fun displayBanner(
        playerId: String,
        round: Int,
        turnColor: RGBA,
    ) {
        displayedBanners += Banner(playerId, round, turnColor)
    }

    override suspend fun announcePlayerTurn(
        playerId: String,
        round: Int,
        turnColor: RGBA,
    ) {
        announcedTurns += Banner(playerId, round, turnColor)
    }

    override fun displayVictory(playerId: String) {
        displayedPlayerWins += playerId
    }
}
