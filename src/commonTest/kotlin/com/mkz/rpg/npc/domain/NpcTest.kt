package com.mkz.rpg.npc.domain

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class NpcTest {
    private val theGuy = Npc.Dto(id = "the-guy", row = 10, column = 2, unitId = null, stock = emptyList())

    @Test
    fun `Place should place the npc on its tile when its name and line are given`() {
        // When
        val npc = Npc.place(theGuy)
        // Then
        npc.toDto() shouldBe theGuy
    }

    @Test
    fun `Place should publish the npc placed event, then request the battlefield to occupy its tile, when the npc is placed`() {
        // When
        val (events, _) = Npc.place(theGuy).pullEvents()
        // Then
        events.shouldContainExactly(
            NpcEvent.NpcPlaced(npcId = "the-guy", row = 10, column = 2),
            BattlefieldEvent.RequestOccupyTile(occupantId = "the-guy", row = 10, column = 2),
        )
    }

    @Test
    fun `Place should place a recruitable npc with its unit, in the npc placed event too, when its unit is given`() {
        // Given
        val viking = Npc.Dto(id = "viking", row = 12, column = 2, unitId = "viking", stock = emptyList())
        // When
        val (events, npc) = Npc.place(viking).pullEvents()
        // Then
        npc.toDto() shouldBe viking
        events.shouldContainExactly(
            NpcEvent.NpcPlaced(npcId = "viking", row = 12, column = 2, unitId = "viking"),
            BattlefieldEvent.RequestOccupyTile(occupantId = "viking", row = 12, column = 2),
        )
    }

    @Test
    fun `IsRecruitable should tell whether the npc has a unit`() {
        // Then
        npc(unitId = "viking").isRecruitable().shouldBeTrue()
        npc(unitId = null).isRecruitable().shouldBeFalse()
    }

    @Test
    fun `Place should place a merchant with the items it sells, in their order, when its stock is given`() {
        // Given
        val merchant = Npc.Dto(id = "merchant", row = 14, column = 2, unitId = null, stock = listOf("mana-potion", "health-potion", "hammer"))
        // When
        val (events, npc) = Npc.place(merchant).pullEvents()
        // Then
        npc.toDto() shouldBe merchant
        events.shouldContainExactly(
            NpcEvent.NpcPlaced(npcId = "merchant", row = 14, column = 2),
            BattlefieldEvent.RequestOccupyTile(occupantId = "merchant", row = 14, column = 2),
        )
    }

    @Test
    fun `IsMerchant should tell whether the npc has a stock`() {
        // Then
        npc(stock = listOf("hammer")).isMerchant().shouldBeTrue()
        npc(stock = emptyList()).isMerchant().shouldBeFalse()
    }

    @Test
    fun `Place should place a merchant selling 12 items`() {
        // Given
        val stock = List(12) { index -> "item-$index" }
        // When
        val npc = Npc.place(theGuy.copy(stock = stock))
        // Then
        npc.toDto().stock shouldBe stock
    }

    @Test
    fun `Place should fail when the stock has more than 12 items`() {
        // When
        val result = runCatching { Npc.place(theGuy.copy(stock = List(13) { index -> "item-$index" })) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcStockTooLarge>()
    }

    @Test
    fun `Place should fail when the stock lists an item twice`() {
        // When
        val result = runCatching { Npc.place(theGuy.copy(stock = listOf("hammer", "health-potion", "hammer"))) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcStockItemRepeated>()
    }

    @Test
    fun `Recruit should publish the npc recruited event, with its tile, then request the battlefield to vacate it, when the npc is recruitable`() {
        // When
        val (events, _) = npc(id = "viking", row = 12, column = 2, unitId = "viking").recruit().pullEvents()
        // Then
        events.shouldContainExactly(
            NpcEvent.NpcRecruited(npcId = "viking", row = 12, column = 2),
            BattlefieldEvent.RequestRemoveOccupant(occupantId = "viking"),
        )
    }

    @Test
    fun `Recruit should fail when the npc is not recruitable`() {
        // When
        val result = runCatching { npc(unitId = null).recruit() }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcNotRecruitable>()
    }

    @Test
    fun `IsAt should tell whether the npc stands on the tile`() {
        // Given
        val npc = npc(row = 10, column = 2)
        // Then
        npc.isAt(row = 10, column = 2).shouldBeTrue()
        npc.isAt(row = 10, column = 3).shouldBeFalse()
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // When
        val (events, _) = npc().pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
