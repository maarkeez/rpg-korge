package com.mkz.tessavar.ability.domain

sealed class AbilityError(
    message: String,
) : Throwable(message = message) {
    class InvalidTargeting : AbilityError(message = "Invalid targeting")

    class AbilityEffectDoesNotExist : AbilityError(message = "Ability effect does not exist")

    class EmptyAbilityId : AbilityError(message = "Ability id is empty")

    class NegativeAbilityManaCost : AbilityError(message = "Ability mana cost is negative")

    class AbilityManaCostAboveLimit : AbilityError(message = "Ability mana cost is higher than 999")

    class NegativeAbilityCooldown : AbilityError(message = "Ability cooldown is negative")

    class AbilityCooldownAboveLimit : AbilityError(message = "Ability cooldown is higher than 99")

    class AbilitySummonDurationBelowMinimum : AbilityError(message = "Ability summon duration is lower than 1")

    class AbilitySummonDurationAboveLimit : AbilityError(message = "Ability summon duration is higher than 99")

    class AbilitySummonDurationMissing : AbilityError(message = "Ability deploying a battle unit requires a summon duration")

    class AbilitySummonDurationNotAllowed : AbilityError(message = "Only an ability deploying a battle unit can have a summon duration")

    class AbilityEmptyEffects : AbilityError(message = "At least one effect is required")

    class AbilityEffectsAboveLimit : AbilityError(message = "Maximum of 3 effects are allowed")
}
