package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestDeployBattleUnit
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent.RequestJoinParty
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class RecruitMemberTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val recruitMember = RecruitMember(partyRepository, eventBus)

    @Test
    fun `should request the deployment of the battle unit for the human player on the tile, and then its joining`() {
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
