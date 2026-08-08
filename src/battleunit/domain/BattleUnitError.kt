package battleunit.domain

sealed class BattleUnitError(message: String) : Throwable(message=message) {
    class UnitNotFound: BattleUnitError("Unit not found")
    class PlayerNotFound: BattleUnitError("Player not found")
    class BattlefieldTileCanNotBeOccupied : BattleUnitError("Battlefield tile can not be occupied")
}
