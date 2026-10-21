package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestDeployBattleUnit
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent.RequestRecruitMember
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.RecruitMember
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestRecruitMemberTest {
    private val eventBus = InMemoryEventBus()
    private val partyRepository = InMemoryPartyRepository()
    private val onRequestRecruitMember = OnRequestRecruitMember(RecruitMember(partyRepository, eventBus), eventBus)

    @Test
    fun `should request the deployment of the battle unit for the party player when its recruitment is requested`() {
        // Given
        val requests = mutableListOf<RequestDeployBattleUnit>()
        eventBus.subscribe<RequestDeployBattleUnit> { event -> requests += event }
        partyRepository.save(party(playerId = "human", battleUnitIds = listOf("knight-1")))
        eventBus.publish(RequestRecruitMember(battleUnitId = "viking", unitId = "viking", row = 12, column = 2))
        // When
        eventBus.dispatch()
        // Then
        requests shouldBe listOf(RequestDeployBattleUnit(battleUnitId = "viking", unitId = "viking", playerId = "human", row = 12, column = 2))
    }
}
