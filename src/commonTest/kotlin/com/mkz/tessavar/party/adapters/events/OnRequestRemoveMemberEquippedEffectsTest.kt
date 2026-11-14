package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.commands.RemoveMemberEquippedEffects
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestRemoveMemberEquippedEffectsTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onRequestRemoveMemberEquippedEffects =
        OnRequestRemoveMemberEquippedEffects(RemoveMemberEquippedEffects(partyRepository, eventBus), eventBus)

    @Test
    fun `should request the removal of the equipped effects from the party member when it is requested`() {
        // Given
        val requests = mutableListOf<RequestRemoveEquippedEffects>()
        eventBus.subscribe<RequestRemoveEquippedEffects> { event -> requests += event }
        partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        eventBus.publish(RequestRemoveMemberEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer"))
        // When
        eventBus.dispatch()
        // Then
        requests shouldBe listOf(RequestRemoveEquippedEffects(battleUnitId = "knight-1", lootableItemId = "hammer"))
    }
}
