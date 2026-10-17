package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent
import com.mkz.rpg.cpuBrain.usecases.commands.EngageEnemies
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnPartyWalkedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onPartyWalked =
        OnPartyWalked(EngageEnemies(queries.searchEnemyPlayer, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus), eventBus)
    private val engagements = mutableListOf<CpuBrainEvent.CombatEngaged>().also { engaged -> eventBus.subscribe<CpuBrainEvent.CombatEngaged> { engaged += it } }

    @Test
    fun `should engage when the party walks within reach of a cpu battle unit`() {
        // Given
        val human = player(id = "human", type = PlayerTypeDto.HUMAN)
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield("K R", occupants = mapOf('K' to "knight-1", 'R' to "rat-1")))
        eventBus.publish(ExplorationEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1")))
        // When
        eventBus.dispatch()
        // Then
        engagements.shouldContainExactly(CpuBrainEvent.CombatEngaged(playerId = "cpu", engagedPlayerId = "human", battleUnitIds = listOf("rat-1")))
    }
}
