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
import org.mockito.kotlin.whenever

class BattleInfoPresenterTest {
    private val battleInfoView = mock<BattleInfoView>()
    private val battleApi = mock<BattleApi>()
    private val playerApi = mock<PlayerApi>()
    private val searchBattle: SearchBattle = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val eventBus = InMemoryEventBus()
    private val timeline = battleSceneTimeline()
    private val battleInfoPresenter =
        BattleInfoPresenter(
            battleInfoView = battleInfoView,
            battleApi = battleApi,
            playerApi = playerApi,
            timeline = timeline,
            eventBus = eventBus,
        ).also {
            whenever(battleApi.searchBattle).thenReturn(searchBattle)
            whenever(playerApi.searchPlayerById).thenReturn(searchPlayerById)
        }

    @Test
    fun `should display battle info when battle info is updated`() {
        // Given
        val playerName = "Player 1"
        val round = 2
        whenever(searchBattle()).thenReturn(Battle.Dto(currentPlayerTurn = "player-1", currentRound = round, isFinished = false))
        whenever(searchPlayerById("player-1")).thenReturn(Player.Dto(id = "player-1", name = playerName, type = Player.Dto.PlayerTypeDto.HUMAN))
        // When
        battleInfoPresenter.updateBattleInfo()
        // Then
        verify(battleInfoView).displayBattleInfo(playerName = playerName, round = round)
    }

    @Test
    fun `should display player name when player wins`() {
        // Given
        val playerName = "Player 1"
        whenever(searchPlayerById("player-1")).thenReturn(Player.Dto(id = "player-1", name = playerName, type = Player.Dto.PlayerTypeDto.HUMAN))
        // When
        battleInfoPresenter.displayPlayerWin(playerId = "player-1")
        // Then
        verify(battleInfoView).displayPlayerWin(playerName)
    }

    @Test
    fun `should display battle info when player turn starts`() {
        // Given
        val playerName = "Player 1"
        whenever(searchBattle()).thenReturn(Battle.Dto(currentPlayerTurn = "player-1", currentRound = 1, isFinished = false))
        whenever(searchPlayerById("player-1")).thenReturn(Player.Dto(id = "player-1", name = playerName, type = Player.Dto.PlayerTypeDto.HUMAN))
        eventBus.publish(PlayerTurnStarted(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(battleInfoView).displayBattleInfo(playerName = playerName, round = 1)
    }

    @Test
    fun `should display player win when player victory is published`() {
        // Given
        val playerName = "Player 1"
        whenever(searchPlayerById("player-1")).thenReturn(Player.Dto(id = "player-1", name = playerName, type = Player.Dto.PlayerTypeDto.HUMAN))
        eventBus.publish(PlayerVictory(playerId = "player-1"))
        // When
        eventBus.dispatch()
        // Then
        verify(battleInfoView).displayPlayerWin(playerName)
    }

    @Test
    fun `should display the battle info once the previous steps have been played when player turn starts`() {
        // Given
        val playerName = "Player 1"
        whenever(searchBattle()).thenReturn(Battle.Dto(currentPlayerTurn = "player-1", currentRound = 2, isFinished = false))
        whenever(searchPlayerById("player-1")).thenReturn(Player.Dto(id = "player-1", name = playerName, type = Player.Dto.PlayerTypeDto.HUMAN))
        val cpuMove = timeline.playLongStep()
        eventBus.publish(PlayerTurnStarted(playerId = "player-1"))
        eventBus.dispatch()
        verify(battleInfoView, never()).displayBattleInfo(any(), any())
        // When
        cpuMove.complete(Unit)
        // Then
        verify(battleInfoView).displayBattleInfo(playerName = playerName, round = 2)
    }
}
