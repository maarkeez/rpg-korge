package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.PositionDto
import com.mkz.rpg.party.domain.PartyMother.party
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class RestorePartyTest {
    private val searchBattleUnitById = SearchBattleUnitById(InMemoryBattleUnitRepository().apply { listOf("knight-1", "knight-2", "knight-3").forEach { id -> create(battleUnit(id = id)) } })
    private val partyRepository = InMemoryPartyRepository()
    private val restoreParty = RestoreParty(partyRepository, searchBattleUnitById)

    // K leads in the corridor "M L K", after swapping with M: the line is K, M, L.
    private val party =
        party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3"))
            .swapLeader(followerId = "knight-3", positions = mapOf("knight-1" to PositionDto(0, 3), "knight-2" to PositionDto(0, 4), "knight-3" to PositionDto(0, 2)))
            .resolveStep(battleUnitId = "knight-1", row = 0, column = 2)
            .pullEvents()
            .second

    @Test
    fun `should rebuild the same state when it restores the snapshot it took`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-2")).openPause().pullEvents().second)
        // When
        restoreParty(party.toSnapshot())
        // Then
        val restored = partyRepository.search()!!
        restored shouldBe party
        restored.canWalk().shouldBeTrue()
    }

    @Test
    fun `should keep no party when the snapshot has none`() {
        // When
        restoreParty(null)
        // Then
        partyRepository.search().shouldBeNull()
    }

    @Test
    fun `should fail when a member is not a battle unit`() {
        // When
        val result = runCatching { restoreParty(party(battleUnitIds = listOf("knight-1", "ghost-1")).toSnapshot()) }
        // Then
        result.exceptionOrNull()?.message.orEmpty() shouldContain "ghost-1"
    }
}
