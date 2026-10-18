package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import com.mkz.rpg.wandering.usecases.commands.RespawnDefeatedBattleUnits
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnPartyWalkedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val wanderingRepository = InMemoryWanderingRepository()
    private val onPartyWalked = OnPartyWalked(RespawnDefeatedBattleUnits(wanderingRepository, queries.canBattlefieldTileBeOccupied, eventBus), eventBus)

    @Test
    fun `should count the party step for every defeated cpu battle unit when the party walks`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (0 to 0))).defeat("rat-1"))
        eventBus.publish(ExplorationEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1")))
        // When
        eventBus.dispatch()
        // Then
        wanderingRepository
            .search()!!
            .toDto()
            .respawns
            .shouldContainExactly(Wandering.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 1))
    }
}
