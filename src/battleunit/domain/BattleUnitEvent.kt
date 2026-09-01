package battleunit.domain

import shared.domain.DomainEvent

sealed interface BattleUnitEvent: DomainEvent{

    data class BattleUnitDeployed(
        val battleUnitId: String,
        val row: Int,
        val column: Int
    ): BattleUnitEvent

    data class BattleUnitMoved(
        val battleUnitId: String,
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
    ): BattleUnitEvent

    data class AbilityCasted(
        val battleUnitId: String,
        val abilityId: String,
        val row: Int,
        val column: Int
    ): BattleUnitEvent

    data class EffectReceived(
        val battleUnitId: String,
        val effectId: String,
    ): BattleUnitEvent

    data class BattleUnitDamaged(
        val battleUnitId: String,
    ): BattleUnitEvent

    data class BattleUnitHealed(
        val battleUnitId: String,
    ): BattleUnitEvent

    data class BattleUnitTeleported(
        val battleUnitId: String,
    ): BattleUnitEvent

    data class BattleUnitDefeated(
        val playerId: String,
        val battleUnitId: String,
    ): BattleUnitEvent
}
