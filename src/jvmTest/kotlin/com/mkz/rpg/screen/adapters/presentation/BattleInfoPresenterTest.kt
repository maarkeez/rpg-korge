package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battle.usecases.queries.SearchBattle
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class BattleInfoPresenterTest {
    private val battleInfoView = mock<BattleInfoView>()
    private val battlefieldView = mock<BattlefieldView>()
    private val battleApi = mock<BattleApi>()
    private val playerApi = mock<PlayerApi>()
    private val searchBattle: SearchBattle = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val eventBus = InMemoryEventBus()
    private val timeline = battleSceneTimeline()
    private val battleInfoPresenter =
        BattleInfoPresenter(
            battleInfoView = battleInfoView,
            battlefieldView = battlefieldView,
            battleApi = battleApi,
            playerApi = playerApi,
            timeline = timeline,
            eventBus = eventBus,
        ).also {
            whenever(battleApi.searchBattle).thenReturn(searchBattle)
            whenever(playerApi.searchPlayerById).thenReturn(searchPlayerById)
        }

    private fun turnOf(
        type: Player.Dto.PlayerTypeDto,
        playerName: String,
        round: Int = 1,
    ) {
        whenever(searchBattle()).thenReturn(Battle.Dto(currentPlayerTurn = "player-1", currentRound = round, isFinished = false))
        whenever(searchPlayerById("player-1")).thenReturn(Player.Dto(id = "player-1", name = playerName, type = type))
    }

    @Test
    fun `AnnounceTurn should announce the turn and tint the battlefield in blue when it is a human player turn`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human", round = 2)
        // When
        battleInfoPresenter.announceTurn()
        // Then
        verifyBlocking(battleInfoView) { announceTurn(playerName = "Human", round = 2, turnColor = BattleInfoView.HUMAN_TURN_COLOR) }
        verifyBlocking(battlefieldView) { tint(BattleInfoView.HUMAN_TURN_COLOR) }
    }

    @Test
    fun `AnnounceTurn should announce the turn and tint the battlefield in red when it is a cpu player turn`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.CPU, playerName = "CPU", round = 2)
        // When
        battleInfoPresenter.announceTurn()
        // Then
        verifyBlocking(battleInfoView) { announceTurn(playerName = "CPU", round = 2, turnColor = BattleInfoView.CPU_TURN_COLOR) }
        verifyBlocking(battlefieldView) { tint(BattleInfoView.CPU_TURN_COLOR) }
    }

    @Test
    fun `AnnounceTurn should announce the turn when player turn starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human")
        eventBus.publish(PlayerTurnStarted(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verifyBlocking(battleInfoView) { announceTurn(playerName = "Human", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR) }
    }

    @Test
    fun `AnnounceTurn should announce the turn once the previous steps have been played when player turn starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human", round = 2)
        val cpuMove = timeline.playLongStep()
        eventBus.publish(PlayerTurnStarted(playerId = "player-1"))
        eventBus.dispatch()
        verifyBlocking(battleInfoView, never()) { announceTurn(any(), any(), any()) }
        verifyBlocking(battlefieldView, never()) { tint(any()) }
        // When
        cpuMove.complete(Unit)
        // Then
        verifyBlocking(battleInfoView) { announceTurn(playerName = "Human", round = 2, turnColor = BattleInfoView.HUMAN_TURN_COLOR) }
        verifyBlocking(battlefieldView) { tint(BattleInfoView.HUMAN_TURN_COLOR) }
    }

    @Test
    fun `DisplayPlayerWin should display player name when player wins`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Player 1")
        // When
        battleInfoPresenter.displayPlayerWin(playerId = "player-1")
        // Then
        verify(battleInfoView).displayPlayerWin("Player 1")
    }

    @Test
    fun `DisplayPlayerWin should display player win when player victory is published`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Player 1")
        eventBus.publish(PlayerVictory(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(battleInfoView).displayPlayerWin("Player 1")
    }
}
