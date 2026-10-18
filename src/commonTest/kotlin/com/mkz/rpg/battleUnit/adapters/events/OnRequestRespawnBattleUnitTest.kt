package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.RespawnBattleUnit
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestRespawnBattleUnitTest {
    private val eventBus = InMemoryEventBus()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val unitRepository = InMemoryUnitRepository()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onRequestRespawnBattleUnit =
        OnRequestRespawnBattleUnit(
            RespawnBattleUnit(battleUnitRepository, SearchUnitById(unitRepository), CanBattlefieldTileBeOccupied(battlefieldRepository), eventBus),
            eventBus,
        )

    @Test
    fun `should respawn the battle unit when its respawn is requested`() {
        // Given
        val rat = unit(id = "rat", healthPoints = 20)
        unitRepository.create(rat)
        battlefieldRepository.create(battlefield(". . ."))
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat.toDto(), remainingHealthPoints = 0))
        eventBus.publish(BattleUnitEvent.RequestRespawnBattleUnit(battleUnitId = "rat-1", row = 0, column = 2))
        // When
        eventBus.dispatch()
        // Then
        battleUnitRepository.searchById("rat-1")!!.toDto().remainingHealthPoints shouldBe 20
    }
}
