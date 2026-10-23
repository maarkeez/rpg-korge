package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FinishPlayerTurnTest {
    private val battleUnitIds = setOf("battle-unit-1", "battle-unit-2")
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val finishPlayerTurn =
        FinishPlayerTurn(
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should start next player turn when another player remains in the queue`() {
        // Given
        val players = listOf("player-1", "player-2")
        battleRepository.create(battle(players, battleUnitIds))
        // When
        finishPlayerTurn()
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players[1], currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerTurnFinished(players[0], battleUnitIds), BattleEvent.PlayerTurnStarted(players[1], battleUnitIds))
    }

    @Test
    fun `should finish round when the current player is the last one in the queue`() {
        // Given
        val players = listOf("player-1", "player-2")
        val battle =
            battle(players, battleUnitIds)
                .finishPlayerTurn()
                .pullEvents()
                .second
        battleRepository.create(battle)
        // When
        finishPlayerTurn()
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players[1], currentRound = 1, isFinished = false, battleUnitIds = battleUnitIds)
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerTurnFinished(players[1], battleUnitIds), BattleEvent.BattleRoundFinished(1, battleUnitIds))
    }

    @Test
    fun `should not finish the player turn when the battle is finished`() {
        // Given: the cpu turn goes on, but its last battle unit engaged was defeated, which finished the battle.
        val finishedBattle =
            battle(listOf("cpu", "human"), battleUnits = mapOf("knight-1" to "human"))
                .finishWithoutWinner()
                .pullEvents()
                .second
        battleRepository.create(finishedBattle)
        // When
        finishPlayerTurn()
        // Then
        battleRepository.search() shouldBe finishedBattle
        eventBus.shouldNotHavePublishedEvents()
    }
}
