package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
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
            RequestApplyEffect(EffectApplicationDto(source = ApplicationSourceDto.item("health-potion"), target = EffectTargetDto.battleUnit("knight-1"), effectId = "heal")),
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
