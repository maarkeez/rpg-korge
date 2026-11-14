package com.mkz.tessavar.npc.usecases.commands

import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.npc.domain.NpcMother.npc
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import kotlin.test.Test

class TalkToAdjacentNpcTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val talkToAdjacentNpc = TalkToAdjacentNpc(queries.searchParty, queries.searchPosition, queries.searchAdjacentNpc, eventBus)

    private fun givenParty(
        map: String,
        npcs: Map<Char, String> = emptyMap(),
    ) {
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('K' to "knight-1", 'L' to "knight-2") + npcs))
        val rows = map.trimIndent().lines().map { line -> line.split(" ").filter(String::isNotBlank).map(String::single) }
        rows.forEachIndexed { row, tiles ->
            tiles.forEachIndexed { column, tile -> npcs[tile]?.let { npcId -> queries.npcRepository.save(npc(id = npcId, row = row, column = column)) } }
        }
        queries.partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
    }

    @Test
    fun `should request to talk to the npc adjacent to the leader`() {
        // Given
        givenParty(
            """
            . . .
            G K .
            . L .
            """,
            npcs = mapOf('G' to "the-guy"),
        )
        // When
        talkToAdjacentNpc()
        // Then
        eventBus.shouldHavePublishedEvents(PartyEvent.RequestStartTalk(npcId = "the-guy"))
    }

    @Test
    fun `should request to talk to the first npc adjacent to the leader, north, east, south and west, in that order`() {
        // Given
        givenParty(
            """
            . . .
            W K E
            . L .
            """,
            npcs = mapOf('W' to "the-west-guy", 'E' to "the-east-guy"),
        )
        // When
        talkToAdjacentNpc()
        // Then
        eventBus.shouldHavePublishedEvents(PartyEvent.RequestStartTalk(npcId = "the-east-guy"))
    }

    @Test
    fun `should not request to talk to an npc on a diagonal of the leader`() {
        // Given
        givenParty(
            """
            G . .
            . K .
            . L .
            """,
            npcs = mapOf('G' to "the-guy"),
        )
        // When
        talkToAdjacentNpc()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when there is no party`() {
        // When
        talkToAdjacentNpc()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
