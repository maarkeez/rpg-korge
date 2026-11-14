package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battleUnit.usecases.commands.RespawnBattleUnit
import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.unit.adapters.storage.InMemoryUnitRepository
import com.mkz.tessavar.unit.domain.UnitMother.unit
import com.mkz.tessavar.unit.usecases.queries.SearchUnitById
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestRespawnBattleUnitTest {
    private val eventBus = InMemoryEventBus()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val unitRepository = InMemoryUnitRepository()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onRequestRespawnBattleUnit =
        OnRequestRespawnBattleUnit(
            RespawnBattleUnit(battleUnitRepository, SearchUnitById(unitRepository), IsTileVacant(battlefieldRepository), eventBus),
            eventBus,
        )

    @Test
    fun `should respawn the battle unit when its respawn is requested`() {
        // Given
        val rat = unit(id = "rat", baseMaximumHealth = 20)
        unitRepository.create(rat)
        battlefieldRepository.create(battlefield(". . ."))
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat.toDto(), remainingHealth = 0))
        eventBus.publish(BattleUnitEvent.RequestRespawnBattleUnit(battleUnitId = "rat-1", row = 0, column = 2))
        // When
        eventBus.dispatch()
        // Then
        battleUnitRepository.searchById("rat-1")!!.toDto().remainingHealth shouldBe 20
    }
}
