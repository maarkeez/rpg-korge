package com.mkz.rpg.ability.domain

sealed class AbilityError(
    message: String,
) : Throwable(message = message) {
    class InvalidTargeting : AbilityError(message = "Invalid targeting")

    class AbilityEffectDoesNotExist : AbilityError(message = "Ability effect does not exist")

    class EmptyAbilityId : AbilityError(message = "Ability id is empty")

    class EmptyAbilityName : AbilityError(message = "Ability name is empty")

    class AbilityNameTooLong : AbilityError(message = "Ability name is longer than 50 characters")

    class NegativeAbilityCost : AbilityError(message = "Ability cost is negative")

    class AbilityCostAboveLimit : AbilityError(message = "Ability cost cost is higher than 999")

    class NegativeAbilityCooldown : AbilityError(message = "Ability cooldown is negative")

    class AbilityCooldownAboveLimit : AbilityError(message = "Ability cooldown is higher than 99")

    class AbilityTurnsDurationBelowMinimum : AbilityError(message = "Ability turns duration is lower than 1")

    class AbilityTurnsDurationAboveLimit : AbilityError(message = "Ability turns duration is higher than 99")

    class AbilityTurnsDurationMissing : AbilityError(message = "Ability deploying a battle unit requires a turns duration")

    class AbilityTurnsDurationNotAllowed : AbilityError(message = "Only an ability deploying a battle unit can have a turns duration")

    class AbilityEmptyEffects : AbilityError(message = "At least one effect is required")

    class AbilityEffectsAboveLimit : AbilityError(message = "Maximum of 3 effects are allowed")
}
