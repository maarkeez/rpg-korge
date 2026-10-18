package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.WanderingEvent
import com.mkz.rpg.wandering.usecases.commands.WanderAfterRound
import com.mkz.rpg.wandering.usecases.services.WanderingSteps
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleRoundFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.0))
    private val wanderingRepository = InMemoryWanderingRepository()
    private val onBattleRoundFinished =
        OnBattleRoundFinished(
            WanderAfterRound(wanderingRepository, WanderingSteps(wanderingRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus), eventBus),
            eventBus,
        )
    private val wanderedEvents =
        mutableListOf<WanderingEvent.BattleUnitsWanderedAfterRound>().also { wandered -> eventBus.subscribe<WanderingEvent.BattleUnitsWanderedAfterRound> { wandered += it } }

    @Test
    fun `should wander the cpu battle units out of the battle when a battle round finishes`() {
        // Given
        eventBus.publish(BattleEvent.BattleRoundFinished(round = 1, battleUnitIds = setOf("knight-1", "rat-1")))
        // When
        eventBus.dispatch()
        // Then
        wanderedEvents.shouldContainExactly(WanderingEvent.BattleUnitsWanderedAfterRound(battleUnitIds = setOf("knight-1", "rat-1")))
    }
}
