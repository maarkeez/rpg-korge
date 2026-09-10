package com.mkz.rpg.screen.battlefieldHud.domain

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
        val tilesWhereCanCast: Set<TileDto>,
    ) : BattlefieldHudEvent

    data class AbilityDeselected(
        val abilityId: String,
    ) : BattlefieldHudEvent

    data class SelfAbilityCastPreviewed(
        val casterBattleUnitId: String,
        val abilityId: String,
        val castTile: TileDto,
    ) : BattlefieldHudEvent

    data class EnemyAbilityCastPreviewed(
        val casterBattleUnitId: String,
        val abilityId: String,
        val castTile: TileDto,
        val enemyBattleUnitId: String,
    ) : BattlefieldHudEvent
}
