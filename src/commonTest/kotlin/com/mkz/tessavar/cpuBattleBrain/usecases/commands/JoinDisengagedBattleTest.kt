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

class JoinDisengagedBattleTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val joinDisengagedBattle =
        JoinDisengagedBattle(queries.searchHumanPlayer, queries.searchOpponent, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus)

    private val human = player(id = "human", type = PlayerTypeDto.HUMAN)
    private val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)

    private fun givenDisengagedBattle(ratIds: List<String>) {
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        ratIds.forEach { ratId -> queries.battleUnitRepository.create(battleUnit(id = ratId, player = cpu.toDto())) }
        queries.battlefieldRepository.create(
            battlefield(
                """
                . . . . . . K . . . . . . .
                . . S . . . . . . . . . . T
                """,
                occupants = mapOf('K' to "knight-1", 'S' to "rat-2", 'T' to "rat-3"),
            ),
        )
    }

    @Test
    fun `should request the battle to finish with the cpu battle units out of the battle that have an enemy within reach joining it`() {
        // Given
        givenDisengagedBattle(ratIds = listOf("rat-2", "rat-3"))
        // When
        joinDisengagedBattle(battleUnitIds = setOf("knight-1"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestResolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = listOf("rat-2")),
        )
    }

    @Test
    fun `should request the battle to finish without anybody joining when no cpu battle unit out of the battle has an enemy within reach`() {
        // Given
        givenDisengagedBattle(ratIds = listOf("rat-3"))
        // When
        joinDisengagedBattle(battleUnitIds = setOf("knight-1"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestResolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList()),
        )
    }

    @Test
    fun `should not have the cpu battle units left in the battle join it again when the human player left it`() {
        // Given
        givenDisengagedBattle(ratIds = listOf("rat-2"))
        // When
        joinDisengagedBattle(battleUnitIds = setOf("rat-2"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestResolveDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList()),
        )
    }

    @Test
    fun `should request the battle to finish without anybody joining when there is no human player`() {
        // When
        joinDisengagedBattle(battleUnitIds = emptySet())
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.CpuBattleUnitsJoinedAndLeft(playerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestResolveDisengagedBattle(joiningPlayerId = null, joiningBattleUnitIds = emptyList()),
        )
    }
}
