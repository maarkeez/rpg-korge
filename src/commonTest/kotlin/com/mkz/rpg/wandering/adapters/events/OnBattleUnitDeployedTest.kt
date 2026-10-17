package com.mkz.rpg.wandering.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.usecases.commands.ClaimHome
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitDeployedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val wanderingRepository = InMemoryWanderingRepository()
    private val onBattleUnitDeployed = OnBattleUnitDeployed(ClaimHome(wanderingRepository, queries.searchBattleUnitById, queries.searchPlayerById), eventBus)

    @Test
    fun `should remember the tile a cpu battle unit is deployed on as its home`() {
        // Given
        val cpu = player(id = "cpu", type = PlayerTypeDto.CPU)
        queries.playerRepository.create(cpu)
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = cpu.toDto()))
        eventBus.publish(BattleUnitEvent.BattleUnitDeployed(battleUnitId = "rat-1", row = 1, column = 2))
        // When
        eventBus.dispatch()
        // Then
        wanderingRepository.search()!!.homeOf("rat-1") shouldBe Wandering.Dto.HomeDto(battleUnitId = "rat-1", row = 1, column = 2)
    }
}
