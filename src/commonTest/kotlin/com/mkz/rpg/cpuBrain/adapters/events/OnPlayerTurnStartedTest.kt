package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.cpuBrain.usecases.commands.PlayTurn
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnPlayerTurnStartedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onPlayerTurnStarted =
        OnPlayerTurnStarted(
            eventBus = eventBus,
            playTurn = PlayTurn(queries.searchPlayerById, queries.searchBattleUnitsByPlayerId, queries.cpuBrainRepository, eventBus),
        )

    @Test
    fun `should play turn when a player turn starts`() {
        // Given
        val cpu = player(id = "cpu", type = CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        eventBus.publish(BattleEvent.PlayerTurnStarted(playerId = "cpu", battleUnitIds = setOf("rat-1")))
        // When
        eventBus.dispatch()
        // Then
        queries.cpuBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds shouldBe listOf("rat-1")
    }
}
