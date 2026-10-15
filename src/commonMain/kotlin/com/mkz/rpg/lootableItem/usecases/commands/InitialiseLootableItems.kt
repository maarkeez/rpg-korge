package com.mkz.rpg.lootableItem.usecases.commands

import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemError.LootableItemEffectDoesNotExist
import com.mkz.rpg.lootableItem.domain.LootableItemEvent
import com.mkz.rpg.lootableItem.domain.LootableItemRepository
import com.mkz.rpg.lootableItem.usecases.services.LootableItemLoader
import com.mkz.rpg.shared.domain.EventBus

/** Creates every loaded lootable item, unless it already exists, once all its effects exist. */
class InitialiseLootableItems(
    private val lootableItemLoader: LootableItemLoader,
    private val lootableItemRepository: LootableItemRepository,
    private val searchEffectById: SearchEffectById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<LootableItemEvent>()
        lootableItemLoader.loadLootableItems().forEach { lootableItemDto ->
            if (lootableItemRepository.searchById(lootableItemDto.id) != null) return@forEach
            val allEffectsExist = lootableItemDto.lootType.effectIds().all { effectId -> searchEffectById(effectId) != null }
            if (!allEffectsExist) throw LootableItemEffectDoesNotExist()
            val (lootableItemEvents, lootableItem) = LootableItem.create(lootableItemDto).pullEvents()
            lootableItemRepository.create(lootableItem)
            events += lootableItemEvents
        }
        eventBus.publish(events)
    }

    private fun LootableItem.Dto.LootTypeDto.effectIds(): Set<String> = consumable?.effectIds.orEmpty() + equippable?.effectIds.orEmpty()
}
