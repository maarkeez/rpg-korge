package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordItemSold(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(
        lootableItemId: String,
        sellPrice: Int,
    ) {
        recordProductEvent { telemetry, occurredAt -> telemetry.itemSold(lootableItemId, sellPrice, occurredAt) }
    }
}
