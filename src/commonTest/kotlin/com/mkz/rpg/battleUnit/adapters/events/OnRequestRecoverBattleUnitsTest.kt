package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.commands.RecoverBattleUnits
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestRecoverBattleUnitsTest {
    private val eventBus = InMemoryEventBus()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val onRequestRecoverBattleUnits = OnRequestRecoverBattleUnits(RecoverBattleUnits(battleUnitRepository), eventBus)

    @Test
    fun `should recover the battle units when their recovery is requested`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = unit(id = "rat", healthPoints = 20).toDto(), remainingHealthPoints = 10))
        eventBus.publish(BattleUnitEvent.RequestRecoverBattleUnits(battleUnitIds = listOf("rat-1"), percentage = 10))
        // When
        eventBus.dispatch()
        // Then
        battleUnitRepository.searchById("rat-1")!!.toDto().remainingHealthPoints shouldBe 12
    }
}
