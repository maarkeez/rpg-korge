package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

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
