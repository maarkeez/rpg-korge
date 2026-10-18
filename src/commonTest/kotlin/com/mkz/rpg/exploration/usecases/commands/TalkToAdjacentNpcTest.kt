package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.Exploration.Dto.StepDto
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TalkToAdjacentNpcTest {
    private val queries = InMemoryQueries()
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val talkToAdjacentNpc = TalkToAdjacentNpc(explorationRepository, queries.searchPosition, queries.searchNpcAt, eventBus)

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
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1", "knight-2")))
    }

    private fun talkingNpcId() = explorationRepository.search()!!.toDto().talkingNpcId

    @Test
    fun `should talk to the npc next to the leader`() {
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
        talkingNpcId() shouldBe "the-guy"
        eventBus.shouldHavePublishedEvents(ExplorationEvent.NpcTalkStarted(playerId = "human", npcId = "the-guy"))
    }

    @Test
    fun `should talk to the first npc next to the leader, north, east, south and west, in that order`() {
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
        talkingNpcId() shouldBe "the-east-guy"
    }

    @Test
    fun `should not talk to an npc on a diagonal of the leader`() {
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
        talkingNpcId().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not talk while the party is walking`() {
        // Given
        givenParty(
            """
            . . .
            G K .
            . L .
            """,
            npcs = mapOf('G' to "the-guy"),
        )
        explorationRepository.save(explorationRepository.search()!!.walkParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 1))))
        // When
        talkToAdjacentNpc()
        // Then
        talkingNpcId().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not talk while battling`() {
        // Given
        givenParty(
            """
            . . .
            G K .
            . L .
            """,
            npcs = mapOf('G' to "the-guy"),
        )
        explorationRepository.save(
            explorationRepository
                .search()!!
                .pause()
                .pullEvents()
                .second,
        )
        // When
        talkToAdjacentNpc()
        // Then
        talkingNpcId().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
