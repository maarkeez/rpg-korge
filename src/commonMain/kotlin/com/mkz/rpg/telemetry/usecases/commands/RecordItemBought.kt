package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent

class RecordItemBought(
    private val recordProductEvent: RecordProductEvent,
) {
    operator fun invoke(
        lootableItemId: String,
        price: Int,
    ) {
        recordProductEvent { telemetry, occurredAt -> telemetry.itemBought(lootableItemId, price, occurredAt) }
    }
}
