package com.mkz.rpg.screen.battlefieldHud.domain

import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.CastGroupDto
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattlefieldHudEvent : DomainEvent {
    object Idle : BattlefieldHudEvent

    data class SelectedBattleUnit(
        val tile: TileDto,
        val battleUnitId: String,
        val tilesWhereCanBeMoved: Set<TileDto>,
    ) : BattlefieldHudEvent

    data class SelectedBattleUnitAbility(
        val casterTile: TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val castGroupsWhereCanCast: List<CastGroupDto>,
    ) : BattlefieldHudEvent

    data class AbilityDeselected(
        val abilityId: String,
    ) : BattlefieldHudEvent

    /** The player tapped an ability that can't be cast. The HUD state doesn't change. */
    data class AbilityUnavailable(
        val abilityId: String,
        val reason: Reason,
    ) : BattlefieldHudEvent {
        sealed interface Reason {
            data class OnCooldown(
                val turnsLeft: Int,
            ) : Reason

            data class NotEnoughMana(
                val cost: Int,
            ) : Reason

            data object NoCastsLeft : Reason
        }
    }

    data class SelfAbilityCastPreviewed(
        val casterBattleUnitId: String,
        val abilityId: String,
        val castGroup: CastGroupDto,
    ) : BattlefieldHudEvent

    data class EnemyAbilityCastPreviewed(
        val casterBattleUnitId: String,
        val abilityId: String,
        val castGroup: CastGroupDto,
        val enemyBattleUnitId: String,
    ) : BattlefieldHudEvent
}
