package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class OnNpcRecruitedTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onNpcRecruited = OnNpcRecruited(RemoveOccupant(battlefieldRepository, eventBus), eventBus)

    @Test
    fun `should vacate the tile of the npc when it is recruited`() {
        // Given
        battlefieldRepository.create(battlefield(". V .", occupants = mapOf('V' to "viking")))
        eventBus.publish(NpcEvent.NpcRecruited(npcId = "viking", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 1).shouldBeNull()
    }
}
