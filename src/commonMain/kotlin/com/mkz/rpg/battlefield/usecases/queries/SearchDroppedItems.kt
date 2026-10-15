package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

/** The items lying on each tile with dropped items, in the order they were dropped, to display them on the battlefield. */
class SearchDroppedItems(
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(): Map<PositionDto, List<String>> = battlefieldRepository.search()?.droppedItems() ?: emptyMap()
}
