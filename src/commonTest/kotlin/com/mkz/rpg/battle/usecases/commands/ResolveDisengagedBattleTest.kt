package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResolveDisengagedBattleTest {
    private val players = listOf("cpu", "human")
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val resolveDisengagedBattle = ResolveDisengagedBattle(battleRepository, eventBus)

    private fun givenDisengagedBattle() {
        battleRepository.create(battle(players, battleUnits = mapOf("rat-1" to "cpu", "knight-1" to "human")).leave("rat-1"))
    }

    @Test
    fun `should finish the battle without a winner when nobody joins it`() {
        // Given
        givenDisengagedBattle()
        // When
        resolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList())
        // Then
        battleRepository
            .search()!!
            .toDto()
            .isFinished
            .shouldBeTrue()
        eventBus.shouldHavePublishedEvents(BattleEvent.BattleFinishedWithoutWinner)
    }

    @Test
    fun `should go on with the joining battle units in the battle when battle units join it`() {
        // Given
        givenDisengagedBattle()
        // When
        resolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = listOf("rat-2"))
        // Then
        val battle = battleRepository.search()!!.toDto()
        battle.isFinished.shouldBeFalse()
        battle.currentRound shouldBe 1
        battle.battleUnitIds shouldBe setOf("knight-1", "rat-2")
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not finish the battle when the player who left it is defeated`() {
        // Given
        val defeatedBattle =
            battle(players, battleUnits = mapOf("rat-1" to "cpu", "knight-1" to "human"))
                .leave("rat-1")
                .defeatPlayer("cpu")
                .pullEvents()
                .second
        battleRepository.create(defeatedBattle)
        // When
        resolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList())
        // Then
        battleRepository.search() shouldBe defeatedBattle
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not finish the battle again when it is finished`() {
        // Given
        val finishedBattle = battle(players, battleUnits = mapOf("knight-1" to "human")).finishWithoutWinner().pullEvents().second
        battleRepository.create(finishedBattle)
        // When
        resolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList())
        // Then
        battleRepository.search() shouldBe finishedBattle
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when no battle exists`() {
        // When
        resolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList())
        // Then
        battleRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
