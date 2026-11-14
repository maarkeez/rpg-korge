package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import kotlin.test.Test

class JoinAndLeaveBattleTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val joinAndLeaveBattle =
        JoinAndLeaveBattle(queries.searchHumanPlayer, queries.searchOpponent, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus)

    private val human = player(id = "human", type = PlayerTypeDto.HUMAN)
    private val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)

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
        joinAndLeaveBattle(battleUnitIds = setOf("knight-1", "rat-1"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestStartNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = emptyList()),
        )
    }

    @Test
    fun `should have nobody join when no cpu battle unit out of the battle has an enemy within reach`() {
        // Given
        givenBattle()
        // When
        joinAndLeaveBattle(battleUnitIds = setOf("knight-1", "rat-1", "rat-2"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestStartNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
        )
    }

    @Test
    fun `should have the cpu battle units in the battle leave it when they have no enemy within reach`() {
        // Given
        givenBattle()
        // When
        joinAndLeaveBattle(battleUnitIds = setOf("knight-1", "rat-1", "rat-2", "rat-3"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = listOf("rat-3")),
            BattleEvent.RequestStartNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = listOf("rat-3")),
        )
    }

    @Test
    fun `should have nobody join nor leave when there is no human player`() {
        // When
        joinAndLeaveBattle(battleUnitIds = emptySet())
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestStartNextRound(joiningPlayerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
        )
    }

    @Test
    fun `should never have the cpu summons in the battle leave it, even with no enemy within reach`() {
        // Given
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "bat-1", player = cpu.toDto(), remainingSummonDuration = 3))
        queries.battlefieldRepository.create(battlefield("B . # . K", occupants = mapOf('K' to "knight-1", 'B' to "bat-1")))
        // When
        joinAndLeaveBattle(battleUnitIds = setOf("knight-1", "bat-1"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestStartNextRound(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
        )
    }
}
