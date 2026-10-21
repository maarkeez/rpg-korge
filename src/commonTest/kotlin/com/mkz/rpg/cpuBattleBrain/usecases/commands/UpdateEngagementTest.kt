package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import kotlin.test.Test

class UpdateEngagementTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val updateEngagement =
        UpdateEngagement(queries.searchHumanPlayer, queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus)

    private val human = player(id = "human", type = PlayerTypeDto.HUMAN)
    private val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)

    // The rat-1 battles next to the knight. The rat-2 is 5 tiles away from the knight, and the rat-3 8 tiles away.
    private fun givenBattle() {
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        listOf("rat-1", "rat-2", "rat-3").forEach { ratId -> queries.battleUnitRepository.create(battleUnit(id = ratId, player = cpu.toDto())) }
        queries.battlefieldRepository.create(
            battlefield(
                """
                . . . . . R K . . . . . . .
                . . S . . . . . . . . . . T
                """,
                occupants = mapOf('K' to "knight-1", 'R' to "rat-1", 'S' to "rat-2", 'T' to "rat-3"),
            ),
        )
    }

    @Test
    fun `should have the cpu battle units out of the battle join it when they have an enemy within reach`() {
        // Given
        givenBattle()
        // When
        updateEngagement(battleUnitIds = setOf("knight-1", "rat-1"))
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleBrainEvent.EngagementUpdated(playerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = emptyList()))
    }

    @Test
    fun `should update the engagement without anybody joining when no cpu battle unit out of the battle has an enemy within reach`() {
        // Given
        givenBattle()
        // When
        updateEngagement(battleUnitIds = setOf("knight-1", "rat-1", "rat-2"))
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleBrainEvent.EngagementUpdated(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()))
    }

    @Test
    fun `should have the cpu battle units in the battle leave it when they have no enemy within reach`() {
        // Given
        givenBattle()
        // When
        updateEngagement(battleUnitIds = setOf("knight-1", "rat-1", "rat-2", "rat-3"))
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleBrainEvent.EngagementUpdated(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = listOf("rat-3")))
    }

    @Test
    fun `should update the engagement without anybody joining nor leaving when there is no human player`() {
        // When
        updateEngagement(battleUnitIds = emptySet())
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleBrainEvent.EngagementUpdated(playerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()))
    }

    @Test
    fun `should never have the cpu summons in the battle leave it, even with no enemy within reach`() {
        // Given: the bat can not walk to the knight.
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "bat-1", player = cpu.toDto(), remainingTurns = 3))
        queries.battlefieldRepository.create(battlefield("B . # . K", occupants = mapOf('K' to "knight-1", 'B' to "bat-1")))
        // When
        updateEngagement(battleUnitIds = setOf("knight-1", "bat-1"))
        // Then
        eventBus.shouldHavePublishedEvents(CpuBattleBrainEvent.EngagementUpdated(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()))
    }
}
