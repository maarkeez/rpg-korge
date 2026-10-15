package com.mkz.rpg.lootableItem.adapters.events

import com.mkz.rpg.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemEvent
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.lootableItem.usecases.commands.InitialiseLootableItems
import com.mkz.rpg.lootableItem.usecases.services.FakeLootableItemLoader
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseLootableItemsTest {
    private val eventBus = InMemoryEventBus()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val effectRepository = InMemoryEffectRepository().also { it.create(increaseHealthEffect(id = "heal")) }
    private val healthPotion = consumableLootableItem(id = "health-potion", consumable = consumable(effectIds = setOf("heal"))).toDto()
    private val onRequestInitialiseLootableItems =
        OnRequestInitialiseLootableItems(
            InitialiseLootableItems(FakeLootableItemLoader(lootableItems = listOf(healthPotion)), lootableItemRepository, SearchEffectById(effectRepository), eventBus),
            eventBus,
        )

    @Test
    fun `should initialise the lootable items when the request is dispatched`() {
        // Given
        eventBus.publish(LootableItemEvent.RequestInitialiseLootableItems)
        // When
        eventBus.dispatch()
        // Then
        lootableItemRepository.searchById("health-potion")?.toDto() shouldBe healthPotion
    }
}
