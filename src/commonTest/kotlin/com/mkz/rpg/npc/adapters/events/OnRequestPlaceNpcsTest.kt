package com.mkz.rpg.npc.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.NpcEvent
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.npc.usecases.commands.PlaceNpcs
import com.mkz.rpg.npc.usecases.services.FakeNpcLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestPlaceNpcsTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val npcRepository = InMemoryNpcRepository()
    private val theGuy = npc(id = "the-guy").toDto()
    private val onRequestPlaceNpcs =
        OnRequestPlaceNpcs(
            PlaceNpcs(FakeNpcLoader(listOf(theGuy)), npcRepository, queries.isTileVacant, queries.searchLootableItemById, eventBus),
            eventBus,
        )

    @Test
    fun `should place the npcs when the request is dispatched`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        eventBus.publish(NpcEvent.RequestPlaceNpcs)
        // When
        eventBus.dispatch()
        // Then
        npcRepository.searchById("the-guy")?.toDto() shouldBe theGuy
    }
}
