package com.mkz.tessavar.cpuBattleBrain.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.cpuBattleBrain.adapters.storage.InMemoryCpuBattleBrainRepository
import com.mkz.tessavar.cpuBattleBrain.domain.CpuBattleBrainMother.cpuBattleBrain
import com.mkz.tessavar.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class JoinCpuPlayerTurnTest {
    private val queries = InMemoryQueries()
    private val cpuBattleBrainRepository = InMemoryCpuBattleBrainRepository()
    private val joinCpuPlayerTurn = JoinCpuPlayerTurn(cpuBattleBrainRepository, queries.searchBattleUnitById)

    init {
        val human = player(id = "human", type = PlayerTypeDto.HUMAN).toDto()
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU).toDto()
        queries.battleUnitRepository.create(battleUnit(id = "bat-1", player = cpu, remainingSummonDuration = 5))
        queries.battleUnitRepository.create(battleUnit(id = "bee-1", player = human, remainingSummonDuration = 5))
    }

    @Test
    fun `should have a battle unit deployed for the cpu player while it plays its player turn play it too`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain(playerId = "cpu", battleUnitIds = listOf("goat-1")).requestCast(battleUnitId = "goat-1", abilityId = "bat"))
        // When
        joinCpuPlayerTurn(battleUnitId = "bat-1")
        // Then
        cpuBattleBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldContainExactly("goat-1", "bat-1")
    }

    @Test
    fun `should not have a battle unit of another player play the cpu player turn`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain(playerId = "cpu", battleUnitIds = listOf("goat-1")))
        // When
        joinCpuPlayerTurn(battleUnitId = "bee-1")
        // Then
        cpuBattleBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldContainExactly("goat-1")
    }

    @Test
    fun `should not have a battle unit deployed out of the cpu player turns play any`() {
        // Given
        cpuBattleBrainRepository.save(cpuBattleBrain(playerId = "cpu", battleUnitIds = listOf("goat-1")).finishPlayerTurn().pullEvents().second)
        // When
        joinCpuPlayerTurn(battleUnitId = "bat-1")
        // Then
        cpuBattleBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldBeEmpty()
    }

    @Test
    fun `should do nothing when no cpu player turn was ever played`() {
        // When
        joinCpuPlayerTurn(battleUnitId = "bat-1")
        // Then
        cpuBattleBrainRepository.search().shouldBeNull()
    }
}
