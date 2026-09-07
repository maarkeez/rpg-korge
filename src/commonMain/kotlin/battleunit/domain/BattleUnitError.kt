package battleunit.domain

sealed class BattleUnitError(message: String) : Throwable(message=message) {
    class UnitNotFound: BattleUnitError("Unit not found")
    class PlayerNotFound: BattleUnitError("Player not found")
    class BattlefieldTileCanNotBeOccupied : BattleUnitError("Battlefield tile can not be occupied")
    class MovementDistanceExceedsRemainingSteps : BattleUnitError("Movement distance exceeds remaining steps")
    class MovementDistanceMustBeGreaterThanZero : BattleUnitError("Movement distance must be greater than zero")
    class BattleUnitCanNotCastAbility : BattleUnitError("Battle unit can not cast ability")
    class InvalidCastPosition : BattleUnitError("Invalid cast position")
    class AbilityDoesNotExists : BattleUnitError("Ability does not exist")
    class FailedToReceiveAbilityEffects : BattleUnitError("Failed to receive ability effects")
    class RemainingManaPointsBelowZero : BattleUnitError("Remaining mana points must be greater than zero")
    class EffectNotFound : BattleUnitError("Effect not found")
    class NotDelayedEffect : BattleUnitError("Not delayed effect")
}
