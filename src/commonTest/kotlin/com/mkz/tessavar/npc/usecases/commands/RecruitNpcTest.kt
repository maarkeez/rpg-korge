package com.mkz.tessavar.npc.usecases.commands

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent.RequestRemoveOccupant
import com.mkz.tessavar.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.tessavar.npc.domain.NpcEvent.NpcRecruited
import com.mkz.tessavar.npc.domain.NpcMother.npc
import com.mkz.tessavar.party.domain.PartyEvent.RequestRecruitMember
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

class RecruitNpcTest {
    private val npcRepository = InMemoryNpcRepository()
    private val eventBus = FakeEventBus()
    private val recruitNpc = RecruitNpc(npcRepository, eventBus)

    @Test
    fun `should remove the occupant, and then request the party to recruit its battle unit there, when the npc is recruitable`() {
        // Given
        npcRepository.save(npc(id = "viking", row = 12, column = 2, unitId = "viking"))
        // When
        recruitNpc(npcId = "viking")
        // Then
        npcRepository.searchById("viking").shouldBeNull()
        eventBus.publishedEvents.shouldContainExactly(
            NpcRecruited(npcId = "viking", row = 12, column = 2),
            RequestRemoveOccupant(occupantId = "viking"),
            RequestRecruitMember(battleUnitId = "viking", unitId = "viking", row = 12, column = 2),
        )
    }

    @Test
    fun `should do nothing when the npc is not recruitable`() {
        // Given
        npcRepository.save(npc(id = "the-guy"))
        // When
        recruitNpc(npcId = "the-guy")
        // Then
        npcRepository.searchById("the-guy").shouldNotBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the npc does not exist`() {
        // When
        recruitNpc(npcId = "viking")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
