package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.FakeBattleInfoView.BattleInfo
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
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

    // Starts the battle in the given round, with the turn of the player-1, a knight each. The presenter is created once the battle has started,
    // so it only reacts to what the test publishes.
    private fun turnOf(
        type: Player.Dto.PlayerTypeDto,
        round: Int = 1,
    ) {
        apis =
            SubdomainApis(
                terrains = listOf(occupiableTerrain(id = "sand").toDto()),
                units = listOf(unit(id = "knight").toDto()),
                players = listOf(player(id = "player-1", type = type).toDto(), player(id = "player-2").toDto()),
                tiles = listOf(listOf("sand", "sand")),
                deployments =
                    listOf(
                        Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "player-1", row = 0, column = 0),
                        Deployment(battleUnitId = "knight-2", unitId = "knight", playerId = "player-2", row = 0, column = 1),
                    ),
                playerOrder = listOf("player-1", "player-2"),
            ).also(SubdomainApis::initialise)
        repeat(round - 1) { apis.battleApi.startNextRound(null, emptyList(), emptyList()) }
        apis.eventBus.dispatch()
        battleInfoPresenter = BattleInfoPresenter(battleInfoView, battlefieldView, apis.battleApi, apis.playerApi, timeline, apis.eventBus)
    }

    @Test
    fun `AnnounceTurn should announce the turn and tint the battlefield in blue when it is a human player turn`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, round = 2)
        // When
        battleInfoPresenter.announceTurn()
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerId = "player-1", round = 2, turnColor = BattleInfoView.HUMAN_TURN_COLOR))
        battlefieldView.tints.shouldContainExactly(BattleInfoView.HUMAN_TURN_COLOR)
    }

    @Test
    fun `AnnounceTurn should announce the turn and tint the battlefield in red when it is a cpu player turn`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.CPU, round = 2)
        // When
        battleInfoPresenter.announceTurn()
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerId = "player-1", round = 2, turnColor = BattleInfoView.CPU_TURN_COLOR))
        battlefieldView.tints.shouldContainExactly(BattleInfoView.CPU_TURN_COLOR)
    }

    @Test
    fun `AnnounceTurn should announce the turn when player turn starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN)
        apis.eventBus.publish(PlayerTurnStarted(playerId = "player-1", battleUnitIds = emptySet()))
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerId = "player-1", round = 1, turnColor = BattleInfoView.HUMAN_TURN_COLOR))
    }

    @Test
    fun `AnnounceTurn should announce the turn once the previous steps have been played when player turn starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN, round = 2)
        val cpuMove = timeline.playLongStep()
        apis.eventBus.publish(PlayerTurnStarted(playerId = "player-1", battleUnitIds = emptySet()))
        apis.eventBus.dispatch()
        battleInfoView.announcedTurns.shouldBeEmpty()
        battlefieldView.tints.shouldBeEmpty()
        // When
        cpuMove.complete(Unit)
        // Then
        battleInfoView.announcedTurns.shouldContainExactly(BattleInfo(playerId = "player-1", round = 2, turnColor = BattleInfoView.HUMAN_TURN_COLOR))
        battlefieldView.tints.shouldContainExactly(BattleInfoView.HUMAN_TURN_COLOR)
    }

    @Test
    fun `DisplayExploring should display that the player explores when the exploration starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN)
        apis.eventBus.publish(PartyEvent.ExplorationStarted(playerId = "player-1", battleUnitIds = listOf("knight-1"), unitIds = listOf("knight")))
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.explorationDisplays shouldBe 1
    }

    @Test
    fun `AnnounceBattle should announce the battle when it starts`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN)
        apis.eventBus.publish(BattleEvent.BattleStarted)
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.announcedBattles shouldBe 1
    }

    @Test
    fun `AnnounceBattleEnd should announce the end of the battle when it finishes`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN)
        apis.eventBus.publish(BattleEvent.BattleFinished)
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.announcedBattleEnds shouldBe 1
    }

    @Test
    fun `DisplayExploring should display that the player explores when the exploration resumes`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN)
        apis.eventBus.publish(PartyEvent.ExplorationResumed(playerId = "player-1", battleUnitIds = listOf("knight-1")))
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.explorationDisplays shouldBe 1
    }

    @Test
    fun `DisplayPlayerWin should display player name when player wins`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN)
        // When
        battleInfoPresenter.displayPlayerWin(playerId = "player-1")
        // Then
        battleInfoView.displayedPlayerWins.shouldContainExactly("player-1")
    }

    @Test
    fun `DisplayPlayerWin should display player win when player victory is published`() {
        // Given
        turnOf(Player.Dto.PlayerTypeDto.HUMAN)
        apis.eventBus.publish(PlayerVictory(playerId = "player-1"))
        // When
        apis.eventBus.dispatch()
        // Then
        battleInfoView.displayedPlayerWins.shouldContainExactly("player-1")
    }
}
