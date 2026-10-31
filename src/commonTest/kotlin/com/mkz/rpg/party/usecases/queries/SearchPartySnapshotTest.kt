package com.mkz.rpg.party.usecases.queries

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.Party.Dto.PositionDto
import com.mkz.rpg.party.domain.PartyMother.party
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchPartySnapshotTest {
    private val partyRepository = InMemoryPartyRepository()
    private val searchPartySnapshot = SearchPartySnapshot(partyRepository)

    @Test
    fun `should return the members and the line of the party`() {
        // Given
        // K leads in the corridor "M L K", after swapping with M: the line is K, M, L.
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3"))
                .swapLeader(followerId = "knight-3", positions = mapOf("knight-1" to PositionDto(0, 3), "knight-2" to PositionDto(0, 4), "knight-3" to PositionDto(0, 2)))
                .resolveStep(battleUnitId = "knight-1", row = 0, column = 2)
                .pullEvents()
                .second,
        )
        // When
        val result = searchPartySnapshot()
        // Then
        result shouldBe Party.Dto.SnapshotDto(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2", "knight-3"), lineBattleUnitIds = listOf("knight-1", "knight-3", "knight-2"))
    }

    @Test
    fun `should return nothing when there is no party`() {
        // When
        val result = searchPartySnapshot()
        // Then
        result.shouldBeNull()
    }
}
