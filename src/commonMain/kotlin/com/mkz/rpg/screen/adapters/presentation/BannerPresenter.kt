package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinishedWithoutWinner
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.save.domain.SaveEvent.GameLoaded
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
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
            eventBus.subscribe<BattleFinishedWithoutWinner> {
                timeline.play { bannerView.announceBattleFinished() }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { bannerView.displayExploring() }
            },
            eventBus.subscribe<PlayerTurnStarted> {
                announcePlayerTurn()
            },
            eventBus.subscribe<PlayerVictory> { event ->
                displayVictory(event.playerId)
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

    fun displayVictory(playerId: String) {
        timeline.play { bannerView.displayVictory(playerId) }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
