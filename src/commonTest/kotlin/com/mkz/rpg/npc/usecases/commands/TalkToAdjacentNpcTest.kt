package com.mkz.rpg.npc.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import kotlin.test.Test

class TalkToAdjacentNpcTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val talkToAdjacentNpc = TalkToAdjacentNpc(queries.searchParty, queries.searchPosition, queries.searchNpcNextTo, eventBus)

    // The knight-1 leads from the centre of the map, and the knight-2 follows it. Each letter of the npcs is an npc.
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
    fun `should request to talk to the npc next to the leader`() {
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
    fun `should request to talk to the first npc next to the leader, north, east, south and west, in that order`() {
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
