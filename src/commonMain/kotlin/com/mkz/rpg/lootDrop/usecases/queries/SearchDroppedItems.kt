package com.mkz.rpg.lootDrop.usecases.queries

import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropRepository

class SearchDroppedItems(
    private val lootDropRepository: LootDropRepository,
) {
    operator fun invoke(): Map<PositionDto, List<String>> =
        lootDropRepository
            .searchAll()
            .flatMap { lootDrop -> lootDrop.toDto().droppedItems.entries }
            .groupBy(keySelector = { (position, _) -> position }, valueTransform = { (_, lootableItemIds) -> lootableItemIds })
            .mapValues { (_, lootableItemIds) -> lootableItemIds.flatten() }
}
