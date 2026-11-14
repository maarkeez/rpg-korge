package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.commands.InterruptExploration
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeFalse
import kotlin.test.Test

class OnBattleStartedTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onBattleStarted = OnBattleStarted(InterruptExploration(partyRepository, eventBus), eventBus)

    @Test
    fun `should stop exploring when a battle starts`() {
        // Given
        partyRepository.save(party())
        eventBus.publish(BattleEvent.BattleStarted)
        // When
        eventBus.dispatch()
        // Then
        partyRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeFalse()
    }
}
