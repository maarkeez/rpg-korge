package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestDeployBattleUnit
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestJoinParty
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class RecruitMemberTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val recruitMember = RecruitMember(partyRepository, eventBus)

    @Test
    fun `should request the deployment of the battle unit for the party player on the tile, and then its joining`() {
        // Given
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
        // When
        recruitMember(battleUnitId = "viking", unitId = "viking", row = 12, column = 2)
        // Then
        eventBus.publishedEvents.shouldContainExactly(
            RequestDeployBattleUnit(battleUnitId = "viking", unitId = "viking", playerId = "human", row = 12, column = 2),
            RequestJoinParty(battleUnitId = "viking"),
        )
    }

    @Test
    fun `should do nothing when there is no party`() {
        // When
        recruitMember(battleUnitId = "viking", unitId = "viking", row = 12, column = 2)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
