package com.mkz.rpg.unit.domain

import com.mkz.rpg.shared.domain.KnownAbilities

sealed class UnitError(
    message: String,
) : Throwable(message = message) {
    class NegativeStartingGold : UnitError("Unit starting gold is negative")

    class AbilitiesAboveLimit : UnitError("Unit abilities are more than ${KnownAbilities.MAXIMUM}")
}
