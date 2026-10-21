package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import kotlin.test.Test

class RequestBattleLeaveTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val requestBattleLeave = RequestBattleLeave(queries.hasAllBattleUnitsDefeated, eventBus)

    @Test
    fun `should request the battle unit to leave the battle and its player to be defeated when all their battle units are defeated`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "cpu").toDto(), unit = unit(healthPoints = 0).toDto()))
        // When
        requestBattleLeave(playerId = "cpu", battleUnitId = "rat-1")
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestLeaveBattle(battleUnitId = "rat-1"), BattleEvent.RequestDefeatPlayer(playerId = "cpu"))
    }

    @Test
    fun `should only request the battle unit to leave the battle when other battle units of its player are not defeated`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "rat-1", player = player(id = "cpu").toDto(), unit = unit(healthPoints = 0).toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "rat-2", player = player(id = "cpu").toDto(), unit = unit(healthPoints = 10).toDto()))
        // When
        requestBattleLeave(playerId = "cpu", battleUnitId = "rat-1")
        // Then
        eventBus.shouldHavePublishedEvents(BattleEvent.RequestLeaveBattle(battleUnitId = "rat-1"))
    }
}
