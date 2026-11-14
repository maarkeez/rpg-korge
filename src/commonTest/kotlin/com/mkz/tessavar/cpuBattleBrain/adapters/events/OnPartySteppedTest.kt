package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.EngageEnemies
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnPartySteppedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onPartyStepped =
        OnPartyStepped(EngageEnemies(queries.searchOpponent, queries.searchBattleUnitsByPlayerId, queries.searchEnemiesWithinReach, eventBus), eventBus)
    private val engagements = mutableListOf<CpuBattleBrainEvent.EnemiesEngaged>().also { engaged -> eventBus.subscribe<CpuBattleBrainEvent.EnemiesEngaged> { engaged += it } }

    @Test
    fun `should engage when the party steps within reach of a cpu battle unit`() {
        // Given
        val human = player(id = "human", type = PlayerTypeDto.HUMAN)
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battlefieldRepository.create(battlefield("K R", occupants = mapOf('K' to "knight-1", 'R' to "rat-1")))
        eventBus.publish(PartyEvent.PartyStepped(playerId = "human", battleUnitIds = listOf("knight-1")))
        // When
        eventBus.dispatch()
        // Then
        engagements.shouldContainExactly(CpuBattleBrainEvent.EnemiesEngaged(playerId = "cpu", engagedPlayerId = "human", battleUnitIds = listOf("rat-1"), engagedBattleUnitIds = listOf("knight-1")))
    }
}
