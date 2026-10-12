package com.mkz.rpg.battle.usecases.commands

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleError
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class StartFirstRoundTest {
    private val battleRepository = InMemoryBattleRepository()
    private val eventBus = FakeEventBus()
    private val startFirstRound =
        StartFirstRound(
            battleRepository = battleRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create battle when at least two players are provided`() {
        // Given
        val players = listOf("player-1", "player-2")
        // When
        startFirstRound(players = players)
        // Then
        val storedBattle = battleRepository.search()?.toDto()
        storedBattle shouldBe Battle.Dto(currentPlayerTurn = players.first(), currentRound = 1, isFinished = false)
        eventBus.shouldHavePublishedEvents(
            BattleEvent.BattleStarted,
            BattleEvent.PlayerTurnStarted(players.first()),
        )
    }

    @Test
    fun `should throw battle error when fewer than two players are provided`() {
        // Given
        val players = listOf("player-1")
        // When
        val error =
            shouldThrowAny {
                startFirstRound(players = players)
            }
        // Then
        error.shouldBeInstanceOf<BattleError.MinimumTwoPlayersRequired>()
    }
}
