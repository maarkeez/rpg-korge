package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.ApplyMemberEffect
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
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
