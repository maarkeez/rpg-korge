package battlefield.domain

import shared.domain.DomainEvent

sealed interface BattlefieldEvent: DomainEvent {
    object BattlefieldCreated : BattlefieldEvent
    data class BattlefieldTileOccupied(val row: Int, val column: Int, val battlefieldUnitId: String): BattlefieldEvent
    data class OccupantRemoved(val battlefieldUnitId: String): BattlefieldEvent
}
