package com.mkz.tessavar.lootableItem.adapters.events

import com.mkz.tessavar.effect.adapters.storage.InMemoryEffectRepository
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItemEvent
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumable
import com.mkz.tessavar.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.tessavar.lootableItem.usecases.commands.InitialiseLootableItems
import com.mkz.tessavar.lootableItem.usecases.services.FakeLootableItemLoader
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestInitialiseLootableItemsTest {
    private val eventBus = InMemoryEventBus()
    private val lootableItemRepository = InMemoryLootableItemRepository()
    private val effectRepository = InMemoryEffectRepository().also { it.create(healEffect(id = "heal")) }
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
