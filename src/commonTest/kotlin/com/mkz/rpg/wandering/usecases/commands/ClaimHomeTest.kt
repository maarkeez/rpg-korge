package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ClaimHomeTest {
    private val queries = InMemoryQueries()
    private val wanderingRepository = InMemoryWanderingRepository()
    private val claimHome = ClaimHome(wanderingRepository, queries.searchBattleUnitById, queries.searchPlayerById)

    init {
        val human = player(id = "human", type = PlayerTypeDto.HUMAN)
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(human)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", player = human.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = cpu.toDto()))
    }

    @Test
    fun `should remember the tile a cpu battle unit is deployed on as its home`() {
        // When
        claimHome(battleUnitId = "rat-1", row = 1, column = 2)
        // Then
        wanderingRepository.search()!!.homeOf("rat-1") shouldBe Wandering.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2)
    }

    @Test
    fun `should keep the homes of the other cpu battle units`() {
        // Given
        wanderingRepository.save(wandering(homes = mapOf("rat-1" to (1 to 2))))
        // When
        claimHome(battleUnitId = "rat-2", row = 3, column = 4)
        // Then
        wanderingRepository.search()!!.homeOf("rat-1") shouldBe Wandering.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2)
        wanderingRepository.search()!!.homeOf("rat-2") shouldBe Wandering.Dto.HomeDto(battleUnitId = "rat-2", row = 3, column = 4)
    }

    @Test
    fun `should not give a home to a human battle unit`() {
        // When
        claimHome(battleUnitId = "knight-1", row = 1, column = 2)
        // Then
        wanderingRepository
            .search()
            ?.toDto()
            ?.homes
            .orEmpty()
            .shouldBeEmpty()
    }

    @Test
    fun `should not give a home to a battle unit that does not exist`() {
        // When
        claimHome(battleUnitId = "ghost", row = 1, column = 2)
        // Then
        wanderingRepository.search().shouldBeNull()
    }
}
