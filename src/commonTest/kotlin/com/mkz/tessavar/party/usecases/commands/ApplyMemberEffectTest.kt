package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import kotlin.test.Test

class ApplyMemberEffectTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val applyMemberEffect = ApplyMemberEffect(partyRepository, eventBus)

    @Test
    fun `should request the application of the effect from the item to the party member`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        // When
        applyMemberEffect(battleUnitId = "knight-1", lootableItemId = "health-potion", effectId = "heal")
        // Then
        eventBus.shouldHavePublishedEvents(
            RequestApplyEffect(EffectApplicationDto(source = SourceDto.item("health-potion"), recipient = RecipientDto.battleUnit("knight-1"), effectId = "heal", destination = null)),
        )
    }

    @Test
    fun `should do nothing when the battle unit is no party member`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        // When
        applyMemberEffect(battleUnitId = "rat-1", lootableItemId = "health-potion", effectId = "heal")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when there is no party`() {
        // When
        applyMemberEffect(battleUnitId = "knight-1", lootableItemId = "health-potion", effectId = "heal")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
