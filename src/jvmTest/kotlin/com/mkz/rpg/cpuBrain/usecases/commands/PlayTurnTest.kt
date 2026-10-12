package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.cpuBrain.adapters.storage.InMemoryCpuBrainRepository
import com.mkz.rpg.cpuBrain.domain.CpuBrainEvent.CpuBrainInitialized
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlayTurnTest {
    private val searchPlayerById: SearchPlayerById = mock()
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId = mock()
    private val cpuBrainRepository = InMemoryCpuBrainRepository()
    private val eventBus = FakeEventBus()
    private val playTurn =
        PlayTurn(
            searchPlayerById = searchPlayerById,
            searchBattleUnitsByPlayerId = searchBattleUnitsByPlayerId,
            cpuBrainRepository = cpuBrainRepository,
            eventBus = eventBus,
        )

    private val cpuPlayer = player(id = "player-1", type = Player.Dto.PlayerTypeDto.CPU).toDto()

    @Test
    fun `should not play turn when the player does not exist`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(null)
        // When
        playTurn("player-1")
        // Then
        eventBus.shouldNotHavePublishedEvents()
        cpuBrainRepository.search().shouldBeNull()
    }

    @Test
    fun `should not play turn when the player is not a cpu`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(player(id = "player-1", type = Player.Dto.PlayerTypeDto.HUMAN).toDto())
        // When
        playTurn("player-1")
        // Then
        eventBus.shouldNotHavePublishedEvents()
        cpuBrainRepository.search().shouldBeNull()
    }

    @Test
    fun `should start managing the battle units in order when the player is a cpu`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(cpuPlayer)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(
            listOf(battleUnit(id = "battle-unit-1").toDto(), battleUnit(id = "battle-unit-2").toDto()),
        )
        // When
        playTurn("player-1")
        // Then
        cpuBrainRepository
            .search()
            ?.toDto()
            ?.pendingBattleUnitIds
            .shouldContainExactly("battle-unit-1", "battle-unit-2")
        eventBus.shouldHavePublishedEvents(CpuBrainInitialized(playerId = "player-1"))
    }

    @Test
    fun `should finish the turn when the cpu player has no battle units`() {
        // Given
        whenever(searchPlayerById("player-1")).thenReturn(cpuPlayer)
        whenever(searchBattleUnitsByPlayerId("player-1")).thenReturn(emptyList())
        // When
        playTurn("player-1")
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestFinishPlayerTurn)
    }
}
