package com.mkz.tessavar.telemetry.usecases.commands

import com.mkz.tessavar.telemetry.usecases.services.RecordProductEvent

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
