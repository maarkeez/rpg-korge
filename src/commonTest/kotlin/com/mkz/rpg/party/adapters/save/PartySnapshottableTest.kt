package com.mkz.rpg.party.adapters.save

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.PositionDto
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.party.usecases.commands.RestoreParty
import com.mkz.rpg.party.usecases.queries.SearchPartySnapshot
import io.kotest.matchers.shouldBe
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

    private fun snapshottable(partyRepository: PartyRepository) = PartySnapshottable(SearchPartySnapshot(partyRepository), RestoreParty(partyRepository, searchBattleUnitById))

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val snapshot = snapshottable(InMemoryPartyRepository().apply { save(party) }).snapshot()
        val restoredRepository = InMemoryPartyRepository()
        // When
        snapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.search() shouldBe party
    }

    @Test
    fun `Key should be party`() {
        // When
        val result = snapshottable(InMemoryPartyRepository()).key
        // Then
        result shouldBe "party"
    }
}
