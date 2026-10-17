package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import kotlin.test.Test

class EngageEnemiesTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val engageEnemies = EngageEnemies(queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus)

    private val human = player(id = "human", type = PlayerTypeDto.HUMAN)
    private val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)

    // A knight, and two rats: the first one 5 tiles away from the knight, the second one 6 tiles away.
    private fun givenRatsAt(map: String) {
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('K' to "knight-1", 'R' to "rat-1", 'S' to "rat-2")))
    }

    @Test
    fun `should engage with the cpu battle units which have the exploring party within reach`() {
        // Given
        givenRatsAt(". . . . . R K . . . . . S")
        // When
        engageEnemies(exploringPlayerId = "human")
        // Then
        eventBus.shouldHavePublishedEvents(CpuBrainEvent.CombatEngaged(playerId = "cpu", engagedPlayerId = "human", battleUnitIds = listOf("rat-1")))
    }

    @Test
    fun `should not engage when the exploring party is out of reach of every cpu battle unit`() {
        // Given
        givenRatsAt("R . . . . . K . . . . . S")
        // When
        engageEnemies(exploringPlayerId = "human")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
