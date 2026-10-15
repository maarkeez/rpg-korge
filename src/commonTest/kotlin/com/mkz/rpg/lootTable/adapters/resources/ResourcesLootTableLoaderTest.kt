package com.mkz.rpg.lootTable.adapters.resources

import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesLootTableLoaderTest {
    @Test
    fun `should load one loot table per toml file when loot table resources are initialised`() =
        runTest {
            // Given
            val lootTableRoot =
                MemoryVfsMix(
                    "lootTable/tier-1.toml" to
                        """
                        [[entries]]
                        type = "LOOTABLE_ITEM"
                        lootableItemId = "health-potion"
                        weight = 10

                        [[entries]]
                        type = "NOTHING"
                        weight = 5
                        """.trimIndent(),
                )["lootTable"]
            val loader = ResourcesLootTableLoader(lootTableRoot = lootTableRoot)
            // When
            loader.initResources()
            // Then
            loader.loadLootTables().shouldContainExactly(
                lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion", weight = 10), nothingEntry(weight = 5))).toDto(),
            )
        }

    @Test
    fun `should fail with the file when the entry type is unknown`() =
        runTest {
            // Given
            val lootTableRoot = MemoryVfsMix("lootTable/tier-1.toml" to "[[entries]]\ntype = \"GOLD\"\nweight = 5\n")["lootTable"]
            val loader = ResourcesLootTableLoader(lootTableRoot = lootTableRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("lootTable/tier-1.toml").shouldContain("GOLD")
        }

    @Test
    fun `should fail with the file when a lootable item entry has no lootable item`() =
        runTest {
            // Given
            val lootTableRoot = MemoryVfsMix("lootTable/tier-1.toml" to "[[entries]]\ntype = \"LOOTABLE_ITEM\"\nweight = 10\n")["lootTable"]
            val loader = ResourcesLootTableLoader(lootTableRoot = lootTableRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("lootTable/tier-1.toml").shouldContain("lootableItemId")
        }
}
