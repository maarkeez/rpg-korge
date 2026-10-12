package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.StartNextRound
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OnRoundFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRoundFinished =
        OnRoundFinished(
            eventBus = eventBus,
            startNextRound = StartNextRound(battleRepository, eventBus),
        )

    @Test
    fun `should start next round when a battle round is finished`() {
        // Given
        battleRepository.create(battle(players = listOf("player-1", "player-2")))
        eventBus.publish(BattleEvent.BattleRoundFinished(round = 1))
        // When
        eventBus.dispatch()
        // Then
        battleRepository.search()?.toDto()?.currentRound shouldBe 2
    }
}
