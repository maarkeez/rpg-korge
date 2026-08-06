package battlefield.domain

sealed interface BattlefieldEvent {
    object BattlefieldCreated : BattlefieldEvent
    data class BattlefieldTileOccupied(val row: Int, val column: Int, val battlefieldUnitId: String): BattlefieldEvent
    data class OccupantRemoved(val battlefieldUnitId: String): BattlefieldEvent
}
