package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent.BattleFinished
import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext

class BattleInfoPresenter(
    private val battleInfoView: BattleInfoView,
    private val battlefieldView: BattlefieldView,
    private val battleApi: BattleApi,
    private val playerApi: PlayerApi,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) {
    private val subscriptions =
        listOf(
            eventBus.subscribe<ExplorationStarted> {
                timeline.play { battleInfoView.displayExploring() }
            },
            eventBus.subscribe<BattleStarted> {
                timeline.play { battleInfoView.announceBattle() }
            },
            eventBus.subscribe<BattleFinished> {
                timeline.play { battleInfoView.announceBattleEnd() }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { battleInfoView.displayExploring() }
            },
            eventBus.subscribe<PlayerTurnStarted> {
                announceTurn()
            },
            eventBus.subscribe<PlayerVictory> { event ->
                displayPlayerWin(event.playerId)
            },
        )

    // Each function reads what it needs straight away and draws it once the previous timeline steps have been played.

    // The header pops while the battlefield is tinted with the turn colour.
    fun announceTurn() {
        val battle = battleApi.searchBattle()!!
        val player = playerApi.searchPlayerById(battle.currentPlayerTurn)!!
        val turnColor =
            when (player.type) {
                PlayerTypeDto.HUMAN -> BattleInfoView.HUMAN_TURN_COLOR
                PlayerTypeDto.CPU -> BattleInfoView.CPU_TURN_COLOR
            }
        timeline.play {
            val tinting = CoroutineScope(currentCoroutineContext()).launchImmediately { battlefieldView.tint(turnColor) }
            battleInfoView.announceTurn(playerId = player.id, round = battle.currentRound, turnColor = turnColor)
            tinting.join()
        }
    }

    fun displayPlayerWin(playerId: String) {
        timeline.play { battleInfoView.displayPlayerWin(playerId) }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
