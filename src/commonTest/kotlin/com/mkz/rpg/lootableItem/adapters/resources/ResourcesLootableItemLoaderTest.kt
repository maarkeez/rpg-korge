package com.mkz.rpg.lootableItem.adapters.resources

import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto.HEAD
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesLootableItemLoaderTest {
    @Test
    fun `should load one lootable item per toml file when lootable item resources are initialised`() =
        runTest {
            // Given
            val lootableItemRoot =
                MemoryVfsMix(
                    "lootableItem/health-potion.toml" to
                        """
                        name = "Health Potion"
                        price = 10
                        type = "CONSUMABLE"

                        [consumable]
                        effectIds = ["heal"]
                        """.trimIndent(),
                    "lootableItem/helmet.toml" to
                        """
                        name = "Helmet"
                        price = 40
                        type = "EQUIPPABLE"

                        [equippable]
                        effectIds = ["protection"]
                        equipmentZone = "HEAD"
                        """.trimIndent(),
                    "lootableItem/health-potion.png" to "not a toml file",
                )["lootableItem"]
            val loader = ResourcesLootableItemLoader(lootableItemRoot = lootableItemRoot)
            // When
            loader.initResources()
            // Then
            loader.loadLootableItems().shouldContainExactly(
                consumableLootableItem(id = "health-potion", name = "Health Potion", price = 10, consumable = consumable(effectIds = setOf("heal"))).toDto(),
                equippableLootableItem(id = "helmet", name = "Helmet", price = 40, equippable = equippable(effectIds = setOf("protection"), equipmentZone = HEAD)).toDto(),
            )
        }

    @Test
    fun `should fail with the file when the loot type details are missing`() =
        runTest {
            // Given
            val lootableItemRoot = MemoryVfsMix("lootableItem/health-potion.toml" to "name = \"Health Potion\"\nprice = 10\ntype = \"CONSUMABLE\"\n")["lootableItem"]
            val loader = ResourcesLootableItemLoader(lootableItemRoot = lootableItemRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("lootableItem/health-potion.toml").shouldContain("consumable")
        }

    @Test
    fun `should fail with the file when the equipment zone is unknown`() =
        runTest {
            // Given
            val lootableItemRoot =
                MemoryVfsMix(
                    "lootableItem/helmet.toml" to "name = \"Helmet\"\nprice = 40\ntype = \"EQUIPPABLE\"\n\n[equippable]\neffectIds = [\"protection\"]\nequipmentZone = \"TAIL\"\n",
                )["lootableItem"]
            val loader = ResourcesLootableItemLoader(lootableItemRoot = lootableItemRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("lootableItem/helmet.toml").shouldContain("TAIL")
        }

    @Test
    fun `should fail with the file when the price is missing`() =
        runTest {
            // Given
            val lootableItemRoot =
                MemoryVfsMix(
                    "lootableItem/health-potion.toml" to "name = \"Health Potion\"\ntype = \"CONSUMABLE\"\n\n[consumable]\neffectIds = [\"heal\"]\n",
                )["lootableItem"]
            val loader = ResourcesLootableItemLoader(lootableItemRoot = lootableItemRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("lootableItem/health-potion.toml").shouldContain("price")
        }
}
