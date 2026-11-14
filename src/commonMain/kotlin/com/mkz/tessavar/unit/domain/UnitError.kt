package com.mkz.tessavar.unit.domain

sealed class UnitError(
    message: String,
) : Throwable(message = message) {
    class NegativeStartingGold : UnitError("Unit starting gold is negative")

    class StartingAbilitiesAboveMaximum : UnitError("Unit starting abilities are above the maximum abilities")
}
