package com.mkz.tessavar.lootableItem.usecases.commands

import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.effect.domain.EffectMother.increaseMaximumHealthEffect
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItem
import com.mkz.tessavar.lootableItem.domain.LootableItemError.ConsumableEffectTimedWhileEquipped
import com.mkz.tessavar.lootableItem.domain.LootableItemError.EquippableEffectNotTimedWhileEquipped
import com.mkz.tessavar.lootableItem.domain.LootableItemError.LootableItemEffectDoesNotExist
import com.mkz.tessavar.lootableItem.domain.LootableItemEvent.LootableItemCreated
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.equippable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.equippableLootableItem
import com.mkz.tessavar.lootableItem.usecases.services.FakeLootableItemLoader
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
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
        effectRepository.create(healEffect(id = "heal"))
        effectRepository.create(increaseMaximumHealthEffect(id = "maximum-health-20"))
        val healthPotion = consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("heal"))).toDto()
        val helmet = equippableLootableItem(id = "helmet", equippable = equippable(effectIds = setOf("maximum-health-20"))).toDto()
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

    @Test
    fun `should fail when a consumable lootable item effect is timed while equipped`() {
        // Given
        effectRepository.create(increaseMaximumHealthEffect(id = "maximum-health-20"))
        val healthPotion = consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("maximum-health-20"))).toDto()
        val initialiseLootableItems = initialiseLootableItems(loadedLootableItems = listOf(healthPotion))
        // When
        val initialisation = { initialiseLootableItems() }
        // Then
        shouldThrow<ConsumableEffectTimedWhileEquipped> { initialisation() }
    }

    @Test
    fun `should fail when an equippable lootable item effect is not timed while equipped`() {
        // Given
        effectRepository.create(increaseMaximumHealthEffect(id = "maximum-health-20"))
        effectRepository.create(healEffect(id = "heal"))
        val helmet = equippableLootableItem(id = "helmet", equippable = equippable(effectIds = setOf("maximum-health-20", "heal"))).toDto()
        val initialiseLootableItems = initialiseLootableItems(loadedLootableItems = listOf(helmet))
        // When
        val initialisation = { initialiseLootableItems() }
        // Then
        shouldThrow<EquippableEffectNotTimedWhileEquipped> { initialisation() }
    }
}
