package com.mkz.rpg.unit.domain

sealed class UnitError(
    message: String,
) : Throwable(message = message) {
    class NegativeStartingGold : UnitError("Unit starting gold is negative")

    class AbilitiesAboveLimit : UnitError("Unit abilities are above their limit")
}
