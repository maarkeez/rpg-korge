package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattleUnitEvent : DomainEvent {
    data class BattleUnitDeployed(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : BattleUnitEvent

    data class RequestDeployBattleUnit(
        val battleUnitId: String,
        val unitId: String,
        val playerId: String,
        val deployAtRow: Int,
        val deployAtColumn: Int,
    ) : BattleUnitEvent

    data class RequestCastAbility(
        val battleUnitId: String,
        val abilityId: String,
        val castGroup: List<Battlefield.Dto.PositionDto>,
    ) : BattleUnitEvent

    data class RequestMoveBattleUnit(
        val battleUnitId: String,
        val moveToRow: Int,
        val moveToColumn: Int,
    ) : BattleUnitEvent

    data class RequestApplyEffect(
        val application: Effect.EffectApplication,
    ) : BattleUnitEvent

    data class BattleUnitMoved(
        val battleUnitId: String,
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
    ) : BattleUnitEvent

    data class AbilityCasted(
        val battleUnitId: String,
        val abilityId: String,
        val castGroup: List<Battlefield.Dto.PositionDto>,
    ) : BattleUnitEvent

    data class EffectReceived(
        val battleUnitId: String,
        val effectId: String,
    ) : BattleUnitEvent

    data class BattleUnitDamaged(
        val battleUnitId: String,
    ) : BattleUnitEvent

    data class BattleUnitHealed(
        val battleUnitId: String,
    ) : BattleUnitEvent

    data class BattleUnitTeleported(
        val battleUnitId: String,
    ) : BattleUnitEvent

    data class BattleUnitDefeated(
        val playerId: String,
        val battleUnitId: String,
        val defeatedAtRow: Int,
        val defeatedAtColumn: Int,
    ) : BattleUnitEvent
}
