package com.mkz.tessavar.battle.usecases.commands

import com.mkz.tessavar.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.tessavar.battle.domain.BattleEvent.BattleStarted
import com.mkz.tessavar.battle.domain.BattleEvent.PlayerTurnStarted
import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartBattleTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val startBattle = StartBattle(battleRepository, eventBus)

    @Test
    fun `should start the first round with the engaging player when the cpu engages`() {
        // Given
        // When
        startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"), engagedBattleUnitIds = listOf("knight-1"))
        // Then
        val battle = battleRepository.search()?.toDto()
        battle?.currentRound shouldBe 1
        battle?.currentPlayerId shouldBe "cpu"
        eventBus.shouldHavePublishedEvents(BattleStarted, PlayerTurnStarted("cpu", setOf("rat-1", "knight-1")))
    }

    @Test
    fun `should put the engaging and the engaged battle units in the battle when the cpu engages`() {
        // Given
        // When
        startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"), engagedBattleUnitIds = listOf("knight-1"))
        // Then
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe setOf("rat-1", "knight-1")
    }

    @Test
    fun `should not start another battle when a battle is in progress`() {
        // Given
        battleRepository.create(battle(players = listOf("human", "cpu")))
        // When
        startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-1"), engagedBattleUnitIds = listOf("knight-1"))
        // Then
        battleRepository.search()?.toDto()?.currentPlayerId shouldBe "human"
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should start a new battle in place of the finished one when the cpu engages again`() {
        // Given
        battleRepository.create(battle(players = listOf("human", "cpu")).finish().pullEvents().second)
        // When
        startBattle(engagingPlayerId = "cpu", engagedPlayerId = "human", engagingBattleUnitIds = listOf("rat-2"), engagedBattleUnitIds = listOf("knight-1"))
        // Then
        val battle = battleRepository.search()!!.toDto()
        battle.isFinished shouldBe false
        battle.currentPlayerId shouldBe "cpu"
        battle.battleUnitIds shouldBe setOf("rat-2", "knight-1")
        eventBus.shouldHavePublishedEvents(BattleStarted, PlayerTurnStarted("cpu", setOf("rat-2", "knight-1")))
    }
}
