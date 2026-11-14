package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.commands.LeaveParty
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onBattleUnitDefeated = OnBattleUnitDefeated(LeaveParty(partyRepository, eventBus), eventBus)

    @Test
    fun `should remove the battle unit from the party when it is defeated`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "human", battleUnitId = "knight-1", unitId = "knight", defeatedAtRow = 0, defeatedAtColumn = 0))
        // When
        eventBus.dispatch()
        // Then
        partyRepository.search()!!.toDto().battleUnitIds shouldBe listOf("knight-2")
    }
}
