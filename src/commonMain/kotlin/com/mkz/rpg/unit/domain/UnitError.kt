package com.mkz.rpg.unit.domain

import com.mkz.rpg.shared.domain.Gold

sealed class UnitError(
    message: String,
) : Throwable(message = message) {
    class NegativeStartingGold : UnitError("Unit starting gold is negative")

    class StartingGoldAboveLimit : UnitError("Unit starting gold is higher than ${Gold.MAXIMUM}")
}
