package com.mkz.rpg.cpuBattleBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.rpg.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitsWanderedWhileExploringTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onBattleUnitsWanderedWhileExploring =
        OnBattleUnitsWanderedWhileExploring(EngageEnemies(queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus), eventBus)
    private val engagements = mutableListOf<CpuBattleBrainEvent.CombatEngaged>().also { engaged -> eventBus.subscribe<CpuBattleBrainEvent.CombatEngaged> { engaged += it } }

    @Test
    fun `should engage when a cpu battle unit wanders within reach of the party`() {
        // Given
        val human = player(id = "human", type = PlayerTypeDto.HUMAN)
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield("K . R", occupants = mapOf('K' to "knight-1", 'R' to "rat-1")))
        eventBus.publish(CpuWanderBrainEvent.BattleUnitsWanderedWhileExploring(exploringPlayerId = "human"))
        // When
        eventBus.dispatch()
        // Then
        engagements.shouldContainExactly(CpuBattleBrainEvent.CombatEngaged(playerId = "cpu", engagedPlayerId = "human", battleUnitIds = listOf("rat-1")))
    }
}
