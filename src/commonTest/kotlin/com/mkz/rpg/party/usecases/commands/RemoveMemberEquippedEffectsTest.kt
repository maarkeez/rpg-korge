package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
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
