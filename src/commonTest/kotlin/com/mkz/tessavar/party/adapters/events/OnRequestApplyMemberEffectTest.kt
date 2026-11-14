package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.commands.ApplyMemberEffect
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestApplyMemberEffectTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onRequestApplyMemberEffect = OnRequestApplyMemberEffect(ApplyMemberEffect(partyRepository, eventBus), eventBus)

    @Test
    fun `should request the application of the effect to the party member when it is requested`() {
        // Given
        val requests = mutableListOf<RequestApplyEffect>()
        eventBus.subscribe<RequestApplyEffect> { event -> requests += event }
        partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        eventBus.publish(RequestApplyMemberEffect(battleUnitId = "knight-1", lootableItemId = "health-potion", effectId = "heal"))
        // When
        eventBus.dispatch()
        // Then
        requests.map { request -> request.application.effectId } shouldBe listOf("heal")
    }
}
