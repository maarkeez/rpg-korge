package battleunit.domain

import shared.domain.DomainEvent

sealed interface BattleUnitEvent: DomainEvent{
    data class BattleUnitDeployed(val battleUnitId: String, val row: Int, val column: Int): BattleUnitEvent
    data class BattleUnitMoved(
        val battleUnitId: String,
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
    ): BattleUnitEvent
}
