package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartExplorationTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val startExploration = StartExploration(queries.searchHumanPlayer, queries.searchBattleUnitsByPlayerId, partyRepository, eventBus)

    private val human = player(id = "human", type = PlayerTypeDto.HUMAN)
    private val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)

    @Test
    fun `should explore with the battle units of the human player when the exploration starts`() {
        // Given
        queries.playerRepository.create(cpu)
        queries.playerRepository.create(human)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "knight-2", player = human.toDto()))
        // When
        startExploration()
        // Then
        val party = partyRepository.search()!!.toDto()
        party.leaderBattleUnitId shouldBe "knight-1"
        party.battleUnitIds shouldBe listOf("knight-1", "knight-2")
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationStarted(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")))
    }

    @Test
    fun `should not explore when there is no human player`() {
        // Given
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        // When
        startExploration()
        // Then
        partyRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not explore when the human player has no battle units`() {
        // Given
        queries.playerRepository.create(human)
        // When
        startExploration()
        // Then
        partyRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
