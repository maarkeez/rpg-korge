package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartNextRoundTest {
    private val players = listOf("cpu", "human")
    private val battleUnitIds = setOf("rat-1", "knight-1")
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val startNextRound =
        StartNextRound(
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    // The rat-1, of the cpu, battles the knight-1, of the human. The rat-2, of the cpu, is out of the battle.
    private fun givenBattle() {
        battleRepository.create(battle(players, battleUnitIds))
    }

    @Test
    fun `should start next round when a battle exists`() {
        // Given
        givenBattle()
        // When
        startNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList())
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 2, isFinished = false, battleUnitIds = battleUnitIds)
        eventBus.shouldHavePublishedEvents(
            BattleEvent.BattleRoundStarted(2, battleUnitIds),
            BattleEvent.PlayerTurnStarted(players.first(), battleUnitIds),
        )
    }

    @Test
    fun `should start next round with the joining battle units in the battle when battle units join`() {
        // Given
        givenBattle()
        // When
        startNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = emptyList())
        // Then
        val joinedBattleUnitIds = battleUnitIds + "rat-2"
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe joinedBattleUnitIds
        eventBus.shouldHavePublishedEvents(
            BattleEvent.BattleRoundStarted(2, joinedBattleUnitIds),
            BattleEvent.PlayerTurnStarted(players.first(), joinedBattleUnitIds),
        )
    }

    @Test
    fun `should start next round without the leaving battle units when other battle units of their player are left in the battle`() {
        // Given
        givenBattle()
        // When
        startNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = listOf("rat-1"))
        // Then
        val updatedBattleUnitIds = setOf("rat-2", "knight-1")
        battleRepository.search()?.toDto()?.battleUnitIds shouldBe updatedBattleUnitIds
        eventBus.shouldHavePublishedEvents(
            BattleEvent.BattleRoundStarted(2, updatedBattleUnitIds),
            BattleEvent.PlayerTurnStarted(players.first(), updatedBattleUnitIds),
        )
    }

    @Test
    fun `should finish the battle without a winner when the last battle units of a player leave it`() {
        // Given
        givenBattle()
        // When
        startNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = listOf("rat-1"))
        // Then
        val storedBattle = battleRepository.search()!!.toDto()
        storedBattle.isFinished.shouldBeTrue()
        storedBattle.currentRound shouldBe 1
        storedBattle.battleUnitIds shouldBe setOf("knight-1")
        eventBus.shouldHavePublishedEvents(BattleEvent.BattleFinished)
    }

    @Test
    fun `should finish the battle without a winner when only one player has battle units left in it`() {
        // Given
        givenBattle()
        battleRepository.create(battle(players, battleUnits = mapOf("knight-1" to "human")))
        // When
        startNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList())
        // Then
        battleRepository
            .search()!!
            .toDto()
            .isFinished
            .shouldBeTrue()
        eventBus.shouldHavePublishedEvents(BattleEvent.BattleFinished)
    }

    @Test
    fun `should not start next round when no battle exists`() {
        // Given
        // When
        startNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList())
        // Then
        battleRepository.search().shouldBeNull()
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not start the next round when the battle is finished`() {
        // Given: the last battle unit of the cpu vanished as the round finished, which defeated it.
        val finishedBattle = battle(players, battleUnitIds).defeatPlayer("cpu").pullEvents().second
        battleRepository.create(finishedBattle)
        // When
        startNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList())
        // Then
        battleRepository.search() shouldBe finishedBattle
        eventBus.shouldNotHavePublishedEvents()
    }
}
