package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.PauseExploration
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeFalse
import kotlin.test.Test

class OnBattleStartedTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onBattleStarted = OnBattleStarted(PauseExploration(partyRepository, eventBus), eventBus)

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
