package com.mkz.rpg.npc.domain

import com.mkz.rpg.npc.domain.NpcMother.npc
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class NpcTest {
    private val theGuy = Npc.Dto(id = "the-guy", name = "The Guy", line = "G'day bloke!", row = 10, column = 2)

    @Test
    fun `Place should place the npc on its tile when its name and line are given`() {
        // When
        val npc = Npc.place(theGuy)
        // Then
        npc.toDto() shouldBe theGuy
    }

    @Test
    fun `Place should publish the npc placed event when the npc is placed`() {
        // When
        val (events, _) = Npc.place(theGuy).pullEvents()
        // Then
        events.shouldContainExactly(NpcEvent.NpcPlaced(npcId = "the-guy", row = 10, column = 2))
    }

    @Test
    fun `Place should place a recruitable npc with its unit, in the npc placed event too, when its unit is given`() {
        // Given
        val viking = Npc.Dto(id = "viking", name = "Viking", line = "For Valhalla!", row = 12, column = 2, unitId = "viking")
        // When
        val (events, npc) = Npc.place(viking).pullEvents()
        // Then
        npc.toDto() shouldBe viking
        events.shouldContainExactly(NpcEvent.NpcPlaced(npcId = "viking", row = 12, column = 2, unitId = "viking"))
    }

    @Test
    fun `IsRecruitable should tell whether the npc has a unit`() {
        // Then
        npc(unitId = "viking").isRecruitable().shouldBeTrue()
        npc(unitId = null).isRecruitable().shouldBeFalse()
    }

    @Test
    fun `Recruit should publish the npc recruited event, with its tile, when the npc is recruitable`() {
        // When
        val (events, _) = npc(id = "viking", row = 12, column = 2, unitId = "viking").recruit().pullEvents()
        // Then
        events.shouldContainExactly(NpcEvent.NpcRecruited(npcId = "viking", row = 12, column = 2))
    }

    @Test
    fun `Recruit should fail when the npc is not recruitable`() {
        // When
        val result = runCatching { npc(unitId = null).recruit() }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcNotRecruitable>()
    }

    @Test
    fun `Place should fail when the name is empty`() {
        // When
        val result = runCatching { Npc.place(theGuy.copy(name = " ")) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcNameRequired>()
    }

    @Test
    fun `Place should fail when the line is empty`() {
        // When
        val result = runCatching { Npc.place(theGuy.copy(line = "")) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcLineRequired>()
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
