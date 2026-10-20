package com.mkz.rpg.unit.adapters.resources

import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesUnitLoaderTest {
    @Test
    fun `should load one unit per toml file when unit resources are initialised`() =
        runTest {
            // Given
            val unitRoot =
                MemoryVfsMix(
                    "unit/knight.toml" to "name = \"Knight\"\nhealthPoints = 100\nmanaPoints = 30\nmovementRange = 3\nabilities = [\"sword\", \"heal\"]\n",
                    "unit/knight.png" to "not a toml file",
                )["unit"]
            val loader = ResourcesUnitLoader(unitRoot = unitRoot)
            // When
            loader.initResources()
            // Then
            loader.loadUnits().shouldContainExactly(
                unit(id = "knight", name = "Knight", healthPoints = 100, manaPoints = 30, abilities = listOf("sword", "heal"), movementRange = 3).toDto(),
            )
        }

    @Test
    fun `should load the starting gold of a unit, or 0 when it has none`() =
        runTest {
            // Given
            val unitRoot =
                MemoryVfsMix(
                    "unit/viking.toml" to "name = \"Viking\"\nhealthPoints = 150\nmanaPoints = 30\nmovementRange = 3\nabilities = [\"sword\"]\nstartingGold = 100\n",
                    "unit/rat.toml" to "name = \"Rat\"\nhealthPoints = 20\nmanaPoints = 10\nmovementRange = 3\nabilities = [\"sword\"]\n",
                )["unit"]
            val loader = ResourcesUnitLoader(unitRoot = unitRoot)
            // When
            loader.initResources()
            // Then
            loader.loadUnits().associate { unit -> unit.id to unit.startingGold } shouldBe mapOf("viking" to 100, "rat" to 0)
        }

    @Test
    fun `should fail with the file when a field is missing`() =
        runTest {
            // Given
            val unitRoot = MemoryVfsMix("unit/knight.toml" to "name = \"Knight\"\n")["unit"]
            val loader = ResourcesUnitLoader(unitRoot = unitRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("unit/knight.toml")
        }
}
