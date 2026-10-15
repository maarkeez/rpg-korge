package com.mkz.rpg.lootDrop.adapters.resources

import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesLootDropLoaderTest {
    @Test
    fun `should load one loot drop per toml file when loot drop resources are initialised`() =
        runTest {
            // Given
            val lootDropRoot = MemoryVfsMix("lootDrop/bat.toml" to "lootTableIds = [\"tier-1\", \"tier-2\"]\n")["lootDrop"]
            val loader = ResourcesLootDropLoader(lootDropRoot = lootDropRoot)
            // When
            loader.initResources()
            // Then
            loader.loadLootDrops().shouldContainExactly(lootDrop(unitId = "bat", lootTableIds = listOf("tier-1", "tier-2")).toDto())
        }

    @Test
    fun `should fail with the file when the loot tables are missing`() =
        runTest {
            // Given
            val lootDropRoot = MemoryVfsMix("lootDrop/bat.toml" to "lootTables = [\"tier-1\"]\n")["lootDrop"]
            val loader = ResourcesLootDropLoader(lootDropRoot = lootDropRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("lootDrop/bat.toml")
        }
}
