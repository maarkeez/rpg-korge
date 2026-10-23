package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import kotlin.test.Test

class ReengageEnemiesTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val reengageEnemies =
        ReengageEnemies(queries.searchHumanPlayer, queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus)

    private val human = player(id = "human", type = PlayerTypeDto.HUMAN)
    private val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)

    // The rat-1, which battled the knight, was defeated. The rat-2 is 5 tiles away from the knight, and the rat-3 8 tiles
    // away. Only the given rats are battle units of the cpu.
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
        reengageEnemies(battleUnitIds = setOf("knight-1"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.EngagementUpdated(playerId = "cpu", joiningBattleUnitIds = listOf("rat-2"), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestFinishDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = listOf("rat-2")),
        )
    }

    @Test
    fun `should request the battle to finish without anybody joining when no cpu battle unit out of the battle has an enemy within reach`() {
        // Given
        givenDisengagedBattle(ratIds = listOf("rat-3"))
        // When
        reengageEnemies(battleUnitIds = setOf("knight-1"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.EngagementUpdated(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestFinishDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList()),
        )
    }

    @Test
    fun `should not have the cpu battle units left in the battle join it again when the human player left it`() {
        // Given: the knight left the battle, which the rat-2 is still in.
        givenDisengagedBattle(ratIds = listOf("rat-2"))
        // When
        reengageEnemies(battleUnitIds = setOf("rat-2"))
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.EngagementUpdated(playerId = "cpu", joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestFinishDisengagedBattle(joiningPlayerId = "cpu", joiningBattleUnitIds = emptyList()),
        )
    }

    @Test
    fun `should request the battle to finish without anybody joining when there is no human player`() {
        // When
        reengageEnemies(battleUnitIds = emptySet())
        // Then
        eventBus.shouldHavePublishedEvents(
            CpuBattleBrainEvent.EngagementUpdated(playerId = null, joiningBattleUnitIds = emptyList(), leavingBattleUnitIds = emptyList()),
            BattleEvent.RequestFinishDisengagedBattle(joiningPlayerId = null, joiningBattleUnitIds = emptyList()),
        )
    }
}
