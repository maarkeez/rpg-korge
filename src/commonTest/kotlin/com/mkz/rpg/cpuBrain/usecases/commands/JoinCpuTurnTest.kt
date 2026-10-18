package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.cpuBrain.adapters.storage.InMemoryCpuBrainRepository
import com.mkz.rpg.cpuBrain.domain.CpuBrainMother.cpuBrain
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class JoinCpuTurnTest {
    private val queries = InMemoryQueries()
    private val cpuBrainRepository = InMemoryCpuBrainRepository()
    private val joinCpuTurn = JoinCpuTurn(cpuBrainRepository, queries.searchBattleUnitById)

    init {
        val human = player(id = "human", type = PlayerTypeDto.HUMAN).toDto()
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU).toDto()
        queries.battleUnitRepository.create(battleUnit(id = "bat-1", player = cpu, remainingTurns = 5))
        queries.battleUnitRepository.create(battleUnit(id = "bee-1", player = human, remainingTurns = 5))
    }

    @Test
    fun `should have a battle unit deployed for the cpu player while it plays its turn play it too`() {
        // Given
        cpuBrainRepository.save(cpuBrain(playerId = "cpu", battleUnitIds = listOf("goat-1")).requestCast(battleUnitId = "goat-1", abilityId = "bat"))
        // When
        joinCpuTurn(battleUnitId = "bat-1")
        // Then
        cpuBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldContainExactly("goat-1", "bat-1")
    }

    @Test
    fun `should not have a battle unit of another player play the cpu turn`() {
        // Given
        cpuBrainRepository.save(cpuBrain(playerId = "cpu", battleUnitIds = listOf("goat-1")))
        // When
        joinCpuTurn(battleUnitId = "bee-1")
        // Then
        cpuBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldContainExactly("goat-1")
    }

    @Test
    fun `should not have a battle unit deployed out of the cpu turns play any`() {
        // Given
        cpuBrainRepository.save(cpuBrain(playerId = "cpu", battleUnitIds = listOf("goat-1")).finishTurn().pullEvents().second)
        // When
        joinCpuTurn(battleUnitId = "bat-1")
        // Then
        cpuBrainRepository
            .search()!!
            .toDto()
            .pendingBattleUnitIds
            .shouldBeEmpty()
    }

    @Test
    fun `should do nothing when no cpu turn was ever played`() {
        // When
        joinCpuTurn(battleUnitId = "bat-1")
        // Then
        cpuBrainRepository.search().shouldBeNull()
    }
}
