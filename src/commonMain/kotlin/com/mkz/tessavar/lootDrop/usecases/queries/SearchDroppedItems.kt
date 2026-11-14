package com.mkz.tessavar.lootDrop.usecases.queries

import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropRepository

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
