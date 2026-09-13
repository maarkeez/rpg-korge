package com.mkz.rpg.screen.battlefieldHud.domain

import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.AbilityDeselected
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnit
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnitAbility
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.SelfAbilityCastPreviewed

sealed interface BattlefieldHud {
    data class Idle(
        private val events: Set<BattlefieldHudEvent>,
    ) : BattlefieldHud {
        companion object {
            fun create() = Idle(setOf(BattlefieldHudEvent.Idle))
        }

        fun idle() = copy(events = events + BattlefieldHudEvent.Idle)

        fun selectBattleUnit(
            tile: Dto.TileDto,
            battleUnitId: String,
            tilesWhereCanBeMoved: Set<Dto.TileDto>,
        ) = DisplayMovementRange(
            tile = tile,
            battleUnitId = battleUnitId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            events =
                events +
                    SelectedBattleUnit(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                    ),
        )

        fun pullEvents() = events to copy(events = emptySet())
    }

    data class DisplayMovementRange(
        val tile: Dto.TileDto,
        val battleUnitId: String,
        val tilesWhereCanBeMoved: Set<Dto.TileDto>,
        private val events: Set<BattlefieldHudEvent>,
    ) : BattlefieldHud {
        fun pullEvents() = events to copy(events = emptySet())

        fun idle() = Idle(events + BattlefieldHudEvent.Idle)

        fun selectAbility(
            abilityId: String,
            castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        ) = DisplayAbilityCastRange(
            casterTile = tile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            events =
                events +
                    SelectedBattleUnitAbility(
                        casterTile = tile,
                        battleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroupsWhereCanCast = castGroupsWhereCanCast,
                    ),
        )

        fun tilesWhereCanBeMoved(
            tile: Dto.TileDto,
            tilesWhereCanBeMoved: Set<Dto.TileDto>,
        ) = copy(
            tile = tile,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            events =
                events +
                    SelectedBattleUnit(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                    ),
        )
    }

    data class DisplayAbilityCastRange(
        val casterTile: Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val tilesWhereCanBeMoved: Set<Dto.TileDto>,
        val castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        private val events: Set<BattlefieldHudEvent>,
    ) : BattlefieldHud {
        fun pullEvents() = events to copy(events = emptySet())

        fun selectAbility(
            abilityId: String,
            castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        ) = copy(
            abilityId = abilityId,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            events =
                events +
                    SelectedBattleUnitAbility(
                        casterTile = casterTile,
                        battleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroupsWhereCanCast = castGroupsWhereCanCast,
                    ),
        )

        fun deselectAbility() =
            DisplayMovementRange(
                tile = casterTile,
                battleUnitId = battleUnitId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                events =
                    events +
                        AbilityDeselected(abilityId) +
                        SelectedBattleUnit(
                            tile = casterTile,
                            battleUnitId = battleUnitId,
                            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                        ),
            )

        fun previewSelfAbilityCast(castGroup: Dto.CastGroupDto) =
            DisplayAbilityCastPreview(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                castGroupsWhereCanCast = castGroupsWhereCanCast,
                castGroup = castGroup,
                enemyBattleUnitId = null,
                events =
                    events +
                        SelfAbilityCastPreviewed(
                            casterBattleUnitId = battleUnitId,
                            abilityId = abilityId,
                            castGroup = castGroup,
                        ),
            )

        fun idle() = Idle(events + BattlefieldHudEvent.Idle)

        fun previewEnemyAbilityCast(
            castGroup: Dto.CastGroupDto,
            enemyBattleUnitId: String,
        ) = DisplayAbilityCastPreview(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            castGroup = castGroup,
            enemyBattleUnitId = enemyBattleUnitId,
            events =
                events +
                    BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                        casterBattleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroup = castGroup,
                        enemyBattleUnitId = enemyBattleUnitId,
                    ),
        )
    }

    data class DisplayAbilityCastPreview(
        val casterTile: Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val tilesWhereCanBeMoved: Set<Dto.TileDto>,
        val castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        val castGroup: Dto.CastGroupDto,
        val enemyBattleUnitId: String?,
        private val events: Set<BattlefieldHudEvent>,
    ) : BattlefieldHud {
        fun pullEvents() = events to copy(events = emptySet())

        fun idle() = Idle(events + BattlefieldHudEvent.Idle)
    }

    interface Dto {
        data class TileDto(
            val row: Int,
            val column: Int,
        )

        data class CastGroupDto(
            val tiles: List<TileDto>,
        )
    }
}
