package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
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
        eventBus.shouldHavePublishedEvents(BattleEvent.PlayerTurnStarted(players[1], battleUnitIds))
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
        eventBus.shouldHavePublishedEvents(BattleEvent.BattleRoundFinished(1))
    }
}
