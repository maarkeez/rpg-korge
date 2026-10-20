package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.ResumeExploration
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnBattleFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onBattleFinished = OnBattleFinished(ResumeExploration(partyRepository, eventBus), eventBus)

    @Test
    fun `should explore again when a battle finishes`() {
        // Given
        partyRepository.save(party().pause().pullEvents().second)
        eventBus.publish(BattleEvent.BattleFinished)
        // When
        eventBus.dispatch()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeTrue()
    }
}
