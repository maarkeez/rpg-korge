package com.mkz.rpg.lootableItem.usecases.commands

import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto.WHILE_EQUIPPED
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.lootableItem.domain.LootableItem
import com.mkz.rpg.lootableItem.domain.LootableItemError.ConsumableEffectAppliedWhileEquipped
import com.mkz.rpg.lootableItem.domain.LootableItemError.EquippableEffectNotAppliedWhileEquipped
import com.mkz.rpg.lootableItem.domain.LootableItemError.LootableItemEffectDoesNotExist
import com.mkz.rpg.lootableItem.domain.LootableItemEvent
import com.mkz.rpg.lootableItem.domain.LootableItemRepository
import com.mkz.rpg.lootableItem.usecases.services.LootableItemLoader
import com.mkz.rpg.shared.domain.EventBus

/**
 * Creates every loaded lootable item, unless it already exists, once all its effects exist and suit its type: a consumable's effects can not be
 * applied while equipped, and an equippable item's effects must be.
 */
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
            if (consumableEffects.any(::isAppliedWhileEquipped)) throw ConsumableEffectAppliedWhileEquipped()
            if (!equippableEffects.all(::isAppliedWhileEquipped)) throw EquippableEffectNotAppliedWhileEquipped()
            val (lootableItemEvents, lootableItem) = LootableItem.create(lootableItemDto).pullEvents()
            lootableItemRepository.create(lootableItem)
            events += lootableItemEvents
        }
        eventBus.publish(events)
    }

    private fun searchEffect(effectId: String): Effect.Dto = searchEffectById(effectId) ?: throw LootableItemEffectDoesNotExist()

    private fun isAppliedWhileEquipped(effect: Effect.Dto): Boolean = effect.application.type == WHILE_EQUIPPED
}
