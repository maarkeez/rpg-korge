package com.mkz.rpg.lootableItem.usecases.commands

import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto.TimingTypeDto.WHILE_EQUIPPED
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemError.ConsumableEffectTimedWhileEquipped
import com.mkz.rpg.lootableItem.domain.LootableItemError.EquippableEffectNotTimedWhileEquipped
import com.mkz.rpg.lootableItem.domain.LootableItemError.LootableItemEffectDoesNotExist
import com.mkz.rpg.lootableItem.domain.LootableItemEvent
import com.mkz.rpg.lootableItem.domain.LootableItemRepository
import com.mkz.rpg.lootableItem.usecases.services.LootableItemLoader
import com.mkz.rpg.shared.domain.EventBus

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
            val consumableEffects =
                lootableItemDto.lootType.consumable
                    ?.effectIds
                    .orEmpty()
                    .map(::searchEffect)
            val equippableEffects =
                lootableItemDto.lootType.equippable
                    ?.effectIds
                    .orEmpty()
                    .map(::searchEffect)
            if (consumableEffects.any(::isTimedWhileEquipped)) throw ConsumableEffectTimedWhileEquipped()
            if (!equippableEffects.all(::isTimedWhileEquipped)) throw EquippableEffectNotTimedWhileEquipped()
            val (lootableItemEvents, lootableItem) = LootableItem.create(lootableItemDto).pullEvents()
            lootableItemRepository.create(lootableItem)
            events += lootableItemEvents
        }
        eventBus.publish(events)
    }

    private fun searchEffect(effectId: String): Effect.Dto = searchEffectById(effectId) ?: throw LootableItemEffectDoesNotExist()

    private fun isTimedWhileEquipped(effect: Effect.Dto): Boolean = effect.timing.type == WHILE_EQUIPPED
}
