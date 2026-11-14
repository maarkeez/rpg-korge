package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.domain.BattleEvent.BattleFinished
import com.mkz.tessavar.battle.domain.BattleEvent.BattleStarted
import com.mkz.tessavar.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.tessavar.party.domain.PartyEvent.ExplorationResumed
import com.mkz.tessavar.party.domain.PartyEvent.ExplorationStarted
import com.mkz.tessavar.party.domain.PartyEvent.GameOver
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.tessavar.save.domain.SaveEvent.GameLoaded
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Subscription
import com.mkz.tessavar.shared.domain.subscribe
import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext

class BannerPresenter(
    private val bannerView: BannerView,
    private val battlefieldView: BattlefieldView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    private val timeline: GameSceneTimeline,
    eventBus: EventBus,
) {
    private var isGameOver = false

    private val subscriptions =
        listOf(
            eventBus.subscribe<ExplorationStarted> {
                timeline.play { bannerView.displayExploring() }
            },
            eventBus.subscribe<GameLoaded> {
                timeline.play { bannerView.displayExploring() }
            },
            eventBus.subscribe<BattleStarted> {
                timeline.play { bannerView.announceBattle() }
            },
            eventBus.subscribe<BattleFinished> {
                if (!isGameOver) timeline.play { bannerView.announceBattleFinished() }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { bannerView.displayExploring() }
            },
            eventBus.subscribe<PlayerTurnStarted> {
                announcePlayerTurn()
            },
            eventBus.subscribe<GameOver> {
                isGameOver = true
                timeline.play { bannerView.displayGameOver() }
            },
        )

    fun announcePlayerTurn() {
        val battle = battleApi.searchBattle()!!
        val player = playerApi.searchPlayerById(battle.currentPlayerId)!!
        val turnColor =
            when (player.type) {
                PlayerTypeDto.HUMAN -> BannerView.HUMAN_TURN_COLOR
                PlayerTypeDto.CPU -> BannerView.CPU_TURN_COLOR
            }
        timeline.play {
            val tinting = CoroutineScope(currentCoroutineContext()).launchImmediately { battlefieldView.tint(turnColor) }
            bannerView.announcePlayerTurn(playerId = player.id, round = battle.currentRound, turnColor = turnColor)
            tinting.join()
        }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
