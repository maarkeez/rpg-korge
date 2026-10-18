package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.WanderingEvent
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import com.mkz.rpg.wandering.usecases.commands.ResolveWanderingStep
import com.mkz.rpg.wandering.usecases.services.WanderingSteps
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattlefieldTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries(random = FixedDoublesRandom(0.9))
    private val wanderingRepository = InMemoryWanderingRepository()
    private val onBattlefieldTileOccupied =
        OnBattlefieldTileOccupied(
            ResolveWanderingStep(wanderingRepository, WanderingSteps(wanderingRepository, queries.whereShouldWander, queries.isOutsideTerritory, queries.searchPosition, eventBus)),
            eventBus,
        )
    private val wanderedEvents =
        mutableListOf<WanderingEvent.BattleUnitsWanderedWhileExploring>().also { wandered -> eventBus.subscribe<WanderingEvent.BattleUnitsWanderedWhileExploring> { wandered += it } }

    @Test
    fun `should resolve the awaited step when the wandering battle unit occupies its tile`() {
        // Given
        wanderingRepository.save(
            wandering(homes = mapOf("rat-1" to (0 to 0)))
                .startWhileExploring(exploringPlayerId = "human", battleUnitIds = listOf("rat-1"))
                .requestStep(battleUnitId = "rat-1", row = 1, column = 0),
        )
        eventBus.publish(BattlefieldEvent.BattlefieldTileOccupied(row = 1, column = 0, battlefieldUnitId = "rat-1"))
        // When
        eventBus.dispatch()
        // Then
        wanderedEvents.shouldContainExactly(WanderingEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
    }
}
