package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainEvent.CpuBattleBrainInitialized
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class PlayTurnTest {
    private val queries = InMemoryQueries()
    private val cpuBattleBrainRepository = queries.cpuBattleBrainRepository
    private val eventBus = FakeEventBus()
    private val playTurn =
        PlayTurn(
            searchPlayerById = queries.searchPlayerById,
            searchBattleUnitsByPlayerId = queries.searchBattleUnitsByPlayerId,
            cpuBattleBrainRepository = cpuBattleBrainRepository,
            eventBus = eventBus,
        )

    private val cpuPlayer = player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU)

    @Test
    fun `should not play turn when the player does not exist`() {
        // Given
        // When
        playTurn("player-1", setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldNotHavePublishedEvents()
        cpuBattleBrainRepository.search().shouldBeNull()
    }

    @Test
    fun `should not play turn when the player is not a cpu`() {
        // Given
        queries.playerRepository.create(player(id = "player-1", type = Player.Dto.PlayerTypeDto.HUMAN))
        // When
        playTurn("player-1", setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldNotHavePublishedEvents()
        cpuBattleBrainRepository.search().shouldBeNull()
    }

    @Test
    fun `should start managing the battle units in order when the player is a cpu`() {
        // Given
        queries.playerRepository.create(cpuPlayer)
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", player = cpuPlayer.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-2", player = cpuPlayer.toDto()))
        // When
        playTurn("player-1", setOf("battle-unit-1", "battle-unit-2"))
        // Then
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.pendingBattleUnitIds
            .shouldContainExactly("battle-unit-1", "battle-unit-2")
        eventBus.shouldHavePublishedEvents(CpuBattleBrainInitialized(playerId = "player-1"))
    }

    @Test
    fun `should only manage the battle units in the battle when the player is a cpu`() {
        // Given
        queries.playerRepository.create(cpuPlayer)
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", player = cpuPlayer.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-3", player = cpuPlayer.toDto()))
        // When
        playTurn("player-1", setOf("battle-unit-1", "battle-unit-2"))
        // Then
        cpuBattleBrainRepository
            .search()
            ?.toDto()
            ?.pendingBattleUnitIds
            .shouldContainExactly("battle-unit-1")
    }

    @Test
    fun `should finish the turn when the cpu player has no battle units in the battle`() {
        // Given
        queries.playerRepository.create(cpuPlayer)
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-3", player = cpuPlayer.toDto()))
        // When
        playTurn("player-1", setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestFinishPlayerTurn)
    }

    @Test
    fun `should finish the turn when the cpu player has no battle units`() {
        // Given
        queries.playerRepository.create(cpuPlayer)
        // When
        playTurn("player-1", setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestFinishPlayerTurn)
    }
}
