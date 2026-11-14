package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.PlayPlayerTurn
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnPlayerTurnStartedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val onPlayerTurnStarted =
        OnPlayerTurnStarted(
            eventBus = eventBus,
            playPlayerTurn = PlayPlayerTurn(queries.searchPlayerById, queries.searchBattleUnitsByPlayerId, queries.cpuBattleBrainRepository, eventBus),
        )

    @Test
    fun `should play the player turn when a player turn starts`() {
        // Given
        val cpu = player(id = "cpu", type = CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        eventBus.publish(BattleEvent.PlayerTurnStarted(playerId = "cpu", battleUnitIds = setOf("rat-1")))
        // When
        eventBus.dispatch()
        // Then
        queries.cpuBattleBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds shouldBe listOf("rat-1")
    }
}
