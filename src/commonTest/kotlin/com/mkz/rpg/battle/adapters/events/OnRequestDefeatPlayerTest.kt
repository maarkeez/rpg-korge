package com.mkz.rpg.battle.adapters.events

import com.mkz.rpg.battle.adapters.storage.InMemoryBattleRepository
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battle.domain.BattleEvent.PlayerDefeated
import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battle.usecases.commands.DefeatPlayer
import com.mkz.rpg.player.adapters.storage.InMemoryPlayerRepository
import com.mkz.rpg.player.usecases.queries.SearchEnemyPlayer
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnRequestDefeatPlayerTest {
    private val eventBus = InMemoryEventBus()
    private val battleRepository = InMemoryBattleRepository()
    private val onRequestDefeatPlayer = OnRequestDefeatPlayer(DefeatPlayer(SearchEnemyPlayer(InMemoryPlayerRepository()), battleRepository, eventBus), eventBus)
    private val defeatedPlayers = mutableListOf<PlayerDefeated>().also { defeated -> eventBus.subscribe<PlayerDefeated> { defeated += it } }

    @Test
    fun `should defeat the player when it is requested to be defeated`() {
        // Given
        battleRepository.create(battle(players = listOf("player-1", "player-2")))
        eventBus.publish(BattleEvent.RequestDefeatPlayer(playerId = "player-2"))
        // When
        eventBus.dispatch()
        // Then
        defeatedPlayers.shouldContainExactly(PlayerDefeated("player-2"))
    }
}
