package com.mkz.tessavar.inventory.adapters.resources

import com.mkz.tessavar.inventory.domain.Inventory
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class InventoryUnitResourcesTest {
    @Test
    fun `should create an inventory with the starting gold of every unit in the unit resources`() =
        runTest {
            // Given
            val unitLoader = ResourcesUnitLoader().also { loader -> loader.initResources() }
            val units = unitLoader.loadUnits()
            // When
            val failures = units.filter { unit -> runCatching { Inventory.create(unit.id, gold = unit.startingGold) }.isFailure }
            // Then
            units.shouldNotBeEmpty()
            failures.map { unit -> "${unit.id} -> ${unit.startingGold}" }.shouldBeEmpty()
        }
}
