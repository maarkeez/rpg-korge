package com.mkz.rpg.lootableItem.usecases.commands

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemError.LootableItemEffectDoesNotExist
import com.mkz.rpg.lootableItem.domain.LootableItemEvent.LootableItemCreated
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.rpg.lootableItem.usecases.services.FakeLootableItemLoader
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseLootableItemsTest {
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val effectRepository = InMemoryEffectRepository()
    private val eventBus = FakeEventBus()

    private fun initialiseLootableItems(loadedLootableItems: List<LootableItem.Dto>) =
        InitialiseLootableItems(FakeLootableItemLoader(loadedLootableItems), lootableItemRepository, SearchEffectById(effectRepository), eventBus)

    @Test
    fun `should create every loaded lootable item when lootable items are initialised`() {
        // Given
        effectRepository.create(increaseHealthEffect(id = "heal"))
        val healthPotion = consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("heal"))).toDto()
        val helmet = equippableLootableItem(id = "helmet", equippable = equippable(effectIds = setOf("heal"))).toDto()
        val initialiseLootableItems = initialiseLootableItems(loadedLootableItems = listOf(healthPotion, helmet))
        // When
        initialiseLootableItems()
        // Then
        lootableItemRepository.searchById("health-potion")?.toDto() shouldBe healthPotion
        lootableItemRepository.searchById("helmet")?.toDto() shouldBe helmet
        eventBus.shouldHavePublishedEvents(LootableItemCreated("health-potion"), LootableItemCreated("helmet"))
    }

    @Test
    fun `should not create a lootable item again when the lootable item already exists`() {
        // Given
        val healthPotion = consumableLootableItem(id = "health-potion")
        lootableItemRepository.create(healthPotion)
        val initialiseLootableItems = initialiseLootableItems(loadedLootableItems = listOf(consumableLootableItem(id = "health-potion").toDto()))
        // When
        initialiseLootableItems()
        // Then
        lootableItemRepository.searchById("health-potion")?.toDto() shouldBe healthPotion.toDto()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when a consumable lootable item effect does not exist`() {
        // Given
        val healthPotion = consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("missing-effect"))).toDto()
        val initialiseLootableItems = initialiseLootableItems(loadedLootableItems = listOf(healthPotion))
        // When
        val initialisation = { initialiseLootableItems() }
        // Then
        shouldThrow<LootableItemEffectDoesNotExist> { initialisation() }
    }

    @Test
    fun `should fail when an equippable lootable item effect does not exist`() {
        // Given
        val helmet = equippableLootableItem(id = "helmet", equippable = equippable(effectIds = setOf("missing-effect"))).toDto()
        val initialiseLootableItems = initialiseLootableItems(loadedLootableItems = listOf(helmet))
        // When
        val initialisation = { initialiseLootableItems() }
        // Then
        shouldThrow<LootableItemEffectDoesNotExist> { initialisation() }
    }
}
