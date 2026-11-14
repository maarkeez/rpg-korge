package com.mkz.tessavar.unit.adapters.resources

import com.mkz.tessavar.unit.domain.Unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class UnitResourcesTest {
    @Test
    fun `should load every unit in the unit resources`() =
        runTest {
            // Given
            val unitLoader = ResourcesUnitLoader().also { loader -> loader.initResources() }
            val units = unitLoader.loadUnits()
            // When
            val failures = units.mapNotNull { unit -> runCatching { Unit.create(unit) }.exceptionOrNull()?.let { error -> "${unit.id} -> ${error.message}" } }
            // Then
            units.shouldNotBeEmpty()
            failures.shouldBeEmpty()
        }
}
