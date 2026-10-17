package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecoverBattleUnitsTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val recoverBattleUnits = RecoverBattleUnits(battleUnitRepository)
    private val rat = unit(id = "rat", healthPoints = 20).toDto()

    private fun healthOf(battleUnitId: String) = battleUnitRepository.searchById(battleUnitId)!!.toDto().remainingHealthPoints

    @Test
    fun `should recover the percentage of the maximum health of each battle unit`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealthPoints = 10))
        battleUnitRepository.create(battleUnit(id = "rat-2", unit = rat, remainingHealthPoints = 5))
        // When
        recoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10)
        // Then
        healthOf("rat-1") shouldBe 12
        healthOf("rat-2") shouldBe 7
    }

    @Test
    fun `should leave the other battle units as they are`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealthPoints = 10))
        battleUnitRepository.create(battleUnit(id = "rat-2", unit = rat, remainingHealthPoints = 5))
        // When
        recoverBattleUnits(battleUnitIds = listOf("rat-1"), percentage = 10)
        // Then
        healthOf("rat-2") shouldBe 5
    }

    @Test
    fun `should keep a defeated battle unit defeated`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealthPoints = 0))
        // When
        recoverBattleUnits(battleUnitIds = listOf("rat-1"), percentage = 10)
        // Then
        healthOf("rat-1") shouldBe 0
    }

    @Test
    fun `should ignore the battle units that do not exist`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealthPoints = 10))
        // When
        recoverBattleUnits(battleUnitIds = listOf("ghost", "rat-1"), percentage = 10)
        // Then
        healthOf("rat-1") shouldBe 12
    }
}
