package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.FakeBattleInfoView.BattleInfo
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattleInfoPresenterTest {
    private val battleInfoView = FakeBattleInfoView()
    private val battlefieldView = FakeBattlefieldView()
    private val timeline = battleSceneTimeline()
    private lateinit var apis: SubdomainApis
    private lateinit var battleInfoPresenter: BattleInfoPresenter

    // Starts the battle in the given round, with the turn of the player-1. The presenter is created once the battle has started,
    // so it only reacts to what the test publishes.
    private fun turnOf(
        type: Player.Dto.PlayerTypeDto,
        playerName: String,
        round: Int = 1,
    ) {
        apis =
            SubdomainApis(
                players = listOf(player(id = "player-1", name = playerName, type = type).toDto(), player(id = "player-2").toDto()),
                playerOrder = listOf("player-1", "player-2"),
            ).also(SubdomainApis::initialise)
        repeat(round - 1) { apis.battleApi.startNextRound(emptyList()) }
        apis.eventBus.dispatch()
        battleInfoPresenter = BattleInfoPresenter(battleInfoView, battlefieldView, apis.battleApi, apis.playerApi, timeline, apis.eventBus)
    }

    @Test
    fun `AnnounceTurn should announce the turn and tint the battlefield in blue when it is a human player turn`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human", round = 2)
        // When
        battleInfoPresenter.announceTurn()
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerName = "Human", round = 2, turnColor = BattleInfoView.HUMAN_TURN_COLOR))
        battlefieldView.tints.shouldContainExactly(BattleInfoView.HUMAN_TURN_COLOR)
    }

    @Test
    fun `AnnounceTurn should announce the turn and tint the battlefield in red when it is a cpu player turn`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.CPU, playerName = "CPU", round = 2)
        // When
        battleInfoPresenter.announceTurn()
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerName = "CPU", round = 2, turnColor = BattleInfoView.CPU_TURN_COLOR))
        battlefieldView.tints.shouldContainExactly(BattleInfoView.CPU_TURN_COLOR)
    }

    @Test
    fun `AnnounceTurn should announce the turn when player turn starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human")
        apis.eventBus.publish(PlayerTurnStarted(playerId = "player-1", battleUnitIds = emptySet()))
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerName = "Human", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR))
    }

    @Test
    fun `AnnounceTurn should announce the turn once the previous steps have been played when player turn starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human", round = 2)
        val cpuMove = timeline.playLongStep()
        apis.eventBus.publish(PlayerTurnStarted(playerId = "player-1", battleUnitIds = emptySet()))
        apis.eventBus.dispatch()
        battleInfoView.announcedTurns.shouldBeEmpty()
        battlefieldView.tints.shouldBeEmpty()
        // When
        cpuMove.complete(Unit)
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerName = "Human", round = 2, turnColor = BattleInfoView.HUMAN_TURN_COLOR))
        battlefieldView.tints.shouldContainExactly(BattleInfoView.HUMAN_TURN_COLOR)
    }

    @Test
    fun `DisplayExploring should display that the player explores when the exploration starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human")
        apis.eventBus.publish(ExplorationEvent.ExplorationStarted(playerId = "player-1", battleUnitIds = listOf("knight-1")))
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.explorationDisplays shouldBe 1
    }

    @Test
    fun `AnnounceBattle should announce the battle when it starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Human")
        apis.eventBus.publish(BattleEvent.BattleStarted)
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.announcedBattles shouldBe 1
    }

    @Test
    fun `DisplayPlayerWin should display player name when player wins`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Player 1")
        // When
        battleInfoPresenter.displayPlayerWin(playerId = "player-1")
        // Then
        battleInfoView.displayedPlayerWins.shouldContainExactly("Player 1")
    }

    @Test
    fun `DisplayPlayerWin should display player win when player victory is published`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, playerName = "Player 1")
        apis.eventBus.publish(PlayerVictory(playerId = "player-1"))
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.displayedPlayerWins.shouldContainExactly("Player 1")
    }
}
