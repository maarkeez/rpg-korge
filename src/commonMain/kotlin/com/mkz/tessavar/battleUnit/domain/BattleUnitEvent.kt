package com.mkz.tessavar.battleUnit.domain

import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface BattleUnitEvent : DomainEvent {
    data class BattleUnitDeployed(
        val playerId: String,
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : BattleUnitEvent

    object RequestInitialiseBattleUnits : BattleUnitEvent

    data class RequestCastAbility(
        val battleUnitId: String,
        val abilityId: String,
        val castGroup: List<Battlefield.Dto.PositionDto>,
    ) : BattleUnitEvent

    data class RequestWalkBattleUnitInBattle(
        val battleUnitId: String,
        val walkToRow: Int,
        val walkToColumn: Int,
    ) : BattleUnitEvent

    data class RequestWalkBattleUnitOutOfBattle(
        val battleUnitId: String,
        val walkToRow: Int,
        val walkToColumn: Int,
    ) : BattleUnitEvent

    data class RequestSwapBattleUnits(
        val battleUnitId: String,
        val otherBattleUnitId: String,
    ) : BattleUnitEvent

    data class RequestRecoverBattleUnits(
        val battleUnitIds: List<String>,
        val percentage: Int,
    ) : BattleUnitEvent

    data class RequestDeployBattleUnit(
        val battleUnitId: String,
        val unitId: String,
        val playerId: String,
        val row: Int,
        val column: Int,
    ) : BattleUnitEvent

    data class RequestRespawnBattleUnit(
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : BattleUnitEvent

    data class RequestApplyEffect(
        val application: Effect.Dto.EffectApplicationDto,
    ) : BattleUnitEvent

    data class RequestRemoveEquippedEffects(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : BattleUnitEvent

    data class BattleUnitMoved(
        val battleUnitId: String,
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
        val path: List<Battlefield.Dto.PositionDto>,
        val swappedWithBattleUnitId: String? = null,
    ) : BattleUnitEvent

    data class AbilityCasted(
        val battleUnitId: String,
        val abilityId: String,
        val castGroup: List<Battlefield.Dto.PositionDto>,
    ) : BattleUnitEvent

    data class AbilityCastResolved(
        val battleUnitId: String,
        val abilityId: String,
    ) : BattleUnitEvent

    data class EffectReceived(
        val battleUnitId: String,
        val effectId: String,
    ) : BattleUnitEvent

    data class OnPlayerTurnStartedEffectTriggered(
        val battleUnitId: String,
        val effectId: String,
    ) : BattleUnitEvent

    data class BattleUnitDamaged(
        val battleUnitId: String,
    ) : BattleUnitEvent

    data class BattleUnitHealed(
        val battleUnitId: String,
    ) : BattleUnitEvent

    data class BattleUnitManaRestored(
        val battleUnitId: String,
    ) : BattleUnitEvent

    data class EquippedEffectsRemoved(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : BattleUnitEvent

    data class BattleUnitTeleported(
        val battleUnitId: String,
    ) : BattleUnitEvent

    data class AbilityLearned(
        val battleUnitId: String,
        val abilityId: String,
    ) : BattleUnitEvent

    data class BattleUnitVanished(
        val playerId: String,
        val battleUnitId: String,
        val unitId: String,
        val row: Int,
        val column: Int,
    ) : BattleUnitEvent

    data class BattleUnitRespawned(
        val playerId: String,
        val battleUnitId: String,
        val row: Int,
        val column: Int,
    ) : BattleUnitEvent

    data class BattleUnitDefeated(
        val playerId: String,
        val battleUnitId: String,
        val unitId: String,
        val defeatedAtRow: Int,
        val defeatedAtColumn: Int,
    ) : BattleUnitEvent
}
