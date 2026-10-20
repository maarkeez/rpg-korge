package com.mkz.rpg.inventory.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitDeployedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val inventoryRepository = InMemoryInventoryRepository()
    private val onBattleUnitDeployed =
        OnBattleUnitDeployed(CreateInventory(inventoryRepository, queries.searchBattleUnitById, queries.searchPlayerById, queries.searchUnitById), eventBus)

    @Test
    fun `should create the inventory of the battle unit when it is deployed`() {
        // Given
        val human = player(id = "human", type = HUMAN)
        val knight = unit(id = "knight", startingGold = 20)
        queries.playerRepository.create(human)
        queries.unitRepository.create(knight)
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = knight.toDto(), player = human.toDto()))
        eventBus.publish(BattleUnitDeployed(battleUnitId = "knight-1", row = 0, column = 0))
        // When
        eventBus.dispatch()
        // Then
        inventoryRepository.searchByBattleUnitId("knight-1")?.toDto()?.gold shouldBe 20
    }
}
