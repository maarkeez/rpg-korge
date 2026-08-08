package battleunit.domain

import shared.domain.DomainEvent

sealed interface BattleUnitEvent: DomainEvent{
    data class BattleUnitDeployed(val battleUnitId: String, val row: Int, val column: Int): BattleUnitEvent
}
