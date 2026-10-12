package com.mkz.rpg.unit.adapters.resources

import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ResourcesUnitLoaderTest {
    @Test
    fun `should load one unit per toml file when unit resources are initialised`() {
        // Given
        val unitRoot =
            MemoryVfsMix(
                "unit/knight.toml" to "name = \"Knight\"\nhealthPoints = 100\nmanaPoints = 30\nmovementRange = 3\nabilities = [\"sword\", \"heal\"]\n",
                "unit/knight.png" to "not a toml file",
            )["unit"]
        val loader = ResourcesUnitLoader(unitRoot = unitRoot)
        // When
        runBlocking { loader.initResources() }
        // Then
        loader.loadUnits().shouldContainExactly(
            unit(id = "knight", name = "Knight", healthPoints = 100, manaPoints = 30, abilities = listOf("sword", "heal"), movementRange = 3).toDto(),
        )
    }

    @Test
    fun `should fail with the file when a field is missing`() {
        // Given
        val unitRoot = MemoryVfsMix("unit/knight.toml" to "name = \"Knight\"\n")["unit"]
        val loader = ResourcesUnitLoader(unitRoot = unitRoot)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("unit/knight.toml")
    }
}
