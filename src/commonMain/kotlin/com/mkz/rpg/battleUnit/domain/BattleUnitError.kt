package com.mkz.rpg.battleUnit.domain

sealed class BattleUnitError(
    message: String,
) : Throwable(message = message) {
    class UnitNotFound : BattleUnitError("Unit not found")

    class PlayerNotFound : BattleUnitError("Player not found")

    class TileIsNotVacant : BattleUnitError("Tile is not vacant")

    class WalkingDistanceExceedsRemainingSteps : BattleUnitError("Walking distance exceeds remaining steps")

    class WalkingDistanceMustBeGreaterThanZero : BattleUnitError("Walking distance must be greater than zero")

    class BattleUnitCanNotCastAbility : BattleUnitError("Battle unit can not cast ability")

    class InvalidCastPosition : BattleUnitError("Invalid cast position")

    class AbilityDoesNotExists : BattleUnitError("Ability does not exist")

    class FailedToReceiveAbilityEffects : BattleUnitError("Failed to receive ability effects")

    class FailedToResolveEffectTarget : BattleUnitError("Failed to resolve effect target")

    class RemainingManaBelowZero : BattleUnitError("Remaining mana must be greater than zero")

    class EffectNotFound : BattleUnitError("Effect not found")

    class NotOnTurnStarted : BattleUnitError("Not on turn started effect")

    class InvalidEffectApplicationStatus : BattleUnitError("Invalid effect application status")

    class BattleUnitDoesNotExists : BattleUnitError("Battle unit does not exist")

    class EquippedEffectAlreadyApplied : BattleUnitError("Equipped effect already applied")

    class EquippedEffectsNotFound : BattleUnitError("Equipped effects not found")

    class AbilityAlreadyKnown : BattleUnitError("The battle unit already knows the ability")

    class AbilitiesAboveLimit : BattleUnitError("The battle unit can not know more abilities")
}
