package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.color.RGBA

class FakeBannerView : BannerView(MessagesMother.messages()) {
    data class Banner(
        val playerId: String,
        val round: Int,
        val turnColor: RGBA,
    )

    val displayedBanners = mutableListOf<Banner>()
    val announcedTurns = mutableListOf<Banner>()
    var gameOverDisplays = 0
        private set
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

    override fun displayGameOver() {
        gameOverDisplays++
    }
}
