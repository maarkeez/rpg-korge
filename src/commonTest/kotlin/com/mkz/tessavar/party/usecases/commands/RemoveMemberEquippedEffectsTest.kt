package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import kotlin.test.Test

class RemoveMemberEquippedEffectsTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val removeMemberEquippedEffects = RemoveMemberEquippedEffects(partyRepository, eventBus)

    @Test
    fun `should request the removal of the effects of the item from the party member`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        // When
        removeMemberEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer")
        // Then
        eventBus.shouldHavePublishedEvents(RequestRemoveEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer"))
    }

    @Test
    fun `should do nothing when the battle unit is no party member`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        // When
        removeMemberEquippedEffects(battleUnitId = "rat-1", lootableItemId = "hammer")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when there is no party`() {
        // When
        removeMemberEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
