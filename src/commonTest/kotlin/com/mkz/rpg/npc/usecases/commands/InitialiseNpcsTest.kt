package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.lootableItem.domain.LootableItemMother.lootableItem
import com.mkz.rpg.npc.adapters.storage.InMemoryNpcRepository
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcError
import com.mkz.rpg.npc.domain.NpcEvent.NpcPlaced
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.npc.usecases.services.FakeNpcLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class InitialiseNpcsTest {
    private val queries = InMemoryQueries()
    private val npcRepository = InMemoryNpcRepository()
    private val eventBus = FakeEventBus()

    private fun initialiseNpcs(vararg npcs: Npc.Dto) =
        InitialiseNpcs(FakeNpcLoader(npcs.toList()), npcRepository, queries.canBattlefieldTileBeOccupied, queries.searchUnitById, queries.searchLootableItemById, eventBus)()

    @Test
    fun `should place every loaded npc on its tile when the npcs are initialised`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . .\n. . ."))
        val theGuy = npc(id = "the-guy", row = 1, column = 2).toDto()
        val theGirl = npc(id = "the-girl", row = 0, column = 0).toDto()
        // When
        initialiseNpcs(theGuy, theGirl)
        // Then
        npcRepository.searchById("the-guy")?.toDto() shouldBe theGuy
        npcRepository.searchById("the-girl")?.toDto() shouldBe theGirl
        eventBus.shouldHavePublishedEvents(
            NpcPlaced(npcId = "the-guy", row = 1, column = 2),
            NpcPlaced(npcId = "the-girl", row = 0, column = 0),
        )
    }

    @Test
    fun `should place a recruitable npc when its unit exists`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        queries.unitRepository.create(unit(id = "viking"))
        val viking = npc(id = "viking", row = 0, column = 1, unitId = "viking").toDto()
        // When
        initialiseNpcs(viking)
        // Then
        npcRepository.searchById("viking")?.toDto() shouldBe viking
        eventBus.shouldHavePublishedEvents(NpcPlaced(npcId = "viking", row = 0, column = 1, unitId = "viking"))
    }

    @Test
    fun `should fail when the unit of a recruitable npc does not exist`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        val result = runCatching { initialiseNpcs(npc(id = "viking", row = 0, column = 1, unitId = "dragon").toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcUnitNotFound>()
        npcRepository.searchById("viking").shouldBeNull()
    }

    @Test
    fun `should place a merchant when every item it sells exists`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        queries.lootableItemRepository.create(lootableItem(id = "hammer"))
        queries.lootableItemRepository.create(lootableItem(id = "health-potion"))
        val merchant = npc(id = "merchant", row = 0, column = 1, stock = listOf("hammer", "health-potion")).toDto()
        // When
        initialiseNpcs(merchant)
        // Then
        npcRepository.searchById("merchant")?.toDto() shouldBe merchant
        eventBus.shouldHavePublishedEvents(NpcPlaced(npcId = "merchant", row = 0, column = 1))
    }

    @Test
    fun `should fail when an item sold by a merchant does not exist`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        queries.lootableItemRepository.create(lootableItem(id = "hammer"))
        // When
        val result = runCatching { initialiseNpcs(npc(id = "merchant", row = 0, column = 1, stock = listOf("hammer", "dragon-egg")).toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcStockItemNotFound>()
        npcRepository.searchById("merchant").shouldBeNull()
    }

    @Test
    fun `should not place an npc again when it is already placed`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". . ."))
        npcRepository.save(npc(id = "the-guy", row = 0, column = 0))
        // When
        initialiseNpcs(npc(id = "the-guy", row = 0, column = 2).toDto())
        // Then
        npcRepository.searchById("the-guy")?.toDto()?.column shouldBe 0
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when the tile of the npc is void`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". #"))
        // When
        val result = runCatching { initialiseNpcs(npc(id = "the-guy", row = 0, column = 1).toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcTileCanNotBeOccupied>()
        npcRepository.searchById("the-guy").shouldBeNull()
    }

    @Test
    fun `should fail when the tile of the npc is out of the battlefield`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        val result = runCatching { initialiseNpcs(npc(id = "the-guy", row = 3, column = 0).toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcTileCanNotBeOccupied>()
    }
}
