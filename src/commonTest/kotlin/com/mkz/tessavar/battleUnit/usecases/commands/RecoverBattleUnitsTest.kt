package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecoverBattleUnitsTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val recoverBattleUnits = RecoverBattleUnits(battleUnitRepository)
    private val rat = unit(id = "rat", baseMaximumHealth = 20).toDto()

    private fun healthOf(battleUnitId: String) = battleUnitRepository.searchById(battleUnitId)!!.toDto().remainingHealth

    @Test
    fun `should recover the percentage of the maximum health of each battle unit`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealth = 10))
        battleUnitRepository.create(battleUnit(id = "rat-2", unit = rat, remainingHealth = 5))
        // When
        recoverBattleUnits(battleUnitIds = listOf("rat-1", "rat-2"), percentage = 10)
        // Then
        healthOf("rat-1") shouldBe 12
        healthOf("rat-2") shouldBe 7
    }

    @Test
    fun `should leave the other battle units as they are`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealth = 10))
        battleUnitRepository.create(battleUnit(id = "rat-2", unit = rat, remainingHealth = 5))
        // When
        recoverBattleUnits(battleUnitIds = listOf("rat-1"), percentage = 10)
        // Then
        healthOf("rat-2") shouldBe 5
    }

    @Test
    fun `should keep a defeated battle unit defeated`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealth = 0))
        // When
        recoverBattleUnits(battleUnitIds = listOf("rat-1"), percentage = 10)
        // Then
        healthOf("rat-1") shouldBe 0
    }

    @Test
    fun `should ignore the battle units that do not exist`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = rat, remainingHealth = 10))
        // When
        recoverBattleUnits(battleUnitIds = listOf("ghost", "rat-1"), percentage = 10)
        // Then
        healthOf("rat-1") shouldBe 12
    }
}
