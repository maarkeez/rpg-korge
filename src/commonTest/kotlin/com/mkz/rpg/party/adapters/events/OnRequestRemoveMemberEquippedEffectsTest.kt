package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.RemoveMemberEquippedEffects
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
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
