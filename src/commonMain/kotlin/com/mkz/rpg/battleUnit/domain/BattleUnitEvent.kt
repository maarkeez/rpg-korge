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

    object RequestInitialiseBattleUnits : BattleUnitEvent

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

    // Requested outside a battle (e.g.: while exploring), so the walk spends no turn action.
    data class RequestWalkBattleUnit(
        val battleUnitId: String,
        val walkToRow: Int,
        val walkToColumn: Int,
    ) : BattleUnitEvent

    // Requested for the CPU battle units wandering out of the battles, which recover a percentage of their maximum health and mana.
    data class RequestRecoverBattleUnits(
        val battleUnitIds: List<String>,
        val percentage: Int,
    ) : BattleUnitEvent

    data class RequestApplyEffect(
        val application: Effect.Dto.EffectApplicationDto,
    ) : BattleUnitEvent

    // Requested when an item is unequipped, so the battle unit loses the effects it applied while equipped.
    data class RequestRemoveEquippedEffects(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : BattleUnitEvent

    // Path: the tiles walked from the origin (excluded) to the destination (included).
    data class BattleUnitMoved(
        val battleUnitId: String,
        val fromRow: Int,
        val fromColumn: Int,
        val toRow: Int,
        val toColumn: Int,
        val path: List<Battlefield.Dto.PositionDto>,
    ) : BattleUnitEvent

    data class AbilityCasted(
        val battleUnitId: String,
        val abilityId: String,
        val castGroup: List<Battlefield.Dto.PositionDto>,
    ) : BattleUnitEvent

    // Published once every effect of a cast has been applied to its targets.
    data class AbilityCastResolved(
        val battleUnitId: String,
        val abilityId: String,
    ) : BattleUnitEvent

    data class EffectReceived(
        val battleUnitId: String,
        val effectId: String,
    ) : BattleUnitEvent

    // Published every time an effect applied at the start of the turn takes effect (e.g.: venom damage).
    data class OnTurnStartedEffectApplied(
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

    // A summon whose turns are over: it is gone, not defeated. It carries its unit and its tile, as it is deleted.
    data class BattleUnitVanished(
        val playerId: String,
        val battleUnitId: String,
        val unitId: String,
        val row: Int,
        val column: Int,
    ) : BattleUnitEvent

    data class BattleUnitDefeated(
        val playerId: String,
        val battleUnitId: String,
        val defeatedAtRow: Int,
        val defeatedAtColumn: Int,
    ) : BattleUnitEvent
}
