package com.mkz.rpg.party.adapters.save

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.PositionDto
import com.mkz.rpg.party.domain.PartyMother.party
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class PartySnapshottableTest {
    private val searchBattleUnitById = SearchBattleUnitById(InMemoryBattleUnitRepository().apply { listOf("knight-1", "knight-2", "knight-3").forEach { id -> create(battleUnit(id = id)) } })

    // K leads in the corridor "M L K", after swapping with M: the line is K, M, L.
    private val party =
        party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3"))
            .swapLeader(followerId = "knight-3", positions = mapOf("knight-1" to PositionDto(0, 3), "knight-2" to PositionDto(0, 4), "knight-3" to PositionDto(0, 2)))
            .resolveStep(battleUnitId = "knight-1", row = 0, column = 2)
            .pullEvents()
            .second

    private fun snapshot(): String = PartySnapshottable(InMemoryPartyRepository().apply { save(party) }, searchBattleUnitById).snapshot()

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val restoredRepository = InMemoryPartyRepository().apply { save(party(battleUnitIds = listOf("knight-2")).openPause().pullEvents().second) }
        // When
        PartySnapshottable(restoredRepository, searchBattleUnitById).restore(snapshot())
        // Then
        val restored = restoredRepository.search()!!
        restored shouldBe party
        restored.canWalk().shouldBeTrue()
    }

    @Test
    fun `Restore should publish no events when it restores a snapshot`() {
        // Given
        val restoredRepository = InMemoryPartyRepository()
        // When
        PartySnapshottable(restoredRepository, searchBattleUnitById).restore(snapshot())
        // Then
        restoredRepository
            .search()!!
            .pullEvents()
            .first
            .shouldBeEmpty()
    }

    @Test
    fun `Restore should fail when a member is not a battle unit`() {
        // Given
        val snapshot = PartySnapshottable(InMemoryPartyRepository().apply { save(party(battleUnitIds = listOf("knight-1", "ghost-1"))) }, searchBattleUnitById).snapshot()
        // When
        val result = runCatching { PartySnapshottable(InMemoryPartyRepository(), searchBattleUnitById).restore(snapshot) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "ghost-1"
    }
}
