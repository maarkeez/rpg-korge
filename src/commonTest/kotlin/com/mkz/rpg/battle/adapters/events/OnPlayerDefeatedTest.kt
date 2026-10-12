package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.FinishBattle
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnPlayerDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onPlayerDefeated =
        OnPlayerDefeated(
            eventBus = eventBus,
            finishBattle = FinishBattle(battleRepository, eventBus),
        )

    @Test
    fun `should finish battle when a player is defeated`() {
        // Given
        battleRepository.create(battle(players = listOf("player-1", "player-2")).defeatPlayer("player-2").pullEvents().second)
        eventBus.publish(BattleEvent.PlayerDefeated(playerId = "player-2"))
        // When
        eventBus.dispatch()
        // Then
        battleRepository.search()?.toDto()?.isFinished shouldBe true
    }
}
