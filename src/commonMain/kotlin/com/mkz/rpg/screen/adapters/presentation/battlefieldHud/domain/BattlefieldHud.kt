package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain

import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent.AbilityDeselected
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnit
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnitAbility
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent.SelfAbilityCastPreviewed

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
                enemyBattleUnitIds = emptyList(),
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
            enemyBattleUnitIds: List<String>,
        ) = DisplayAbilityCastPreview(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            castGroup = castGroup,
            enemyBattleUnitIds = enemyBattleUnitIds,
            events =
                events +
                    BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                        casterBattleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroup = castGroup,
                        enemyBattleUnitIds = enemyBattleUnitIds,
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
        // The battle units receiving the cast, by row and column. Empty when the cast does not target any enemy.
        val enemyBattleUnitIds: List<String>,
        private val events: Set<BattlefieldHudEvent>,
    ) : BattlefieldHud {
        fun pullEvents() = events to copy(events = emptySet())

        fun idle() = Idle(events + BattlefieldHudEvent.Idle)

        /**
         * Keeps the caster selected. Its movement range is displayed once the cast has been resolved, since the cast
         * changes the caster (e.g.: its mana, or its tile when teleported).
         */
        fun confirmCast() =
            DisplayMovementRange(
                tile = casterTile,
                battleUnitId = battleUnitId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                events = events,
            )

        // Goes back to choosing where to cast the ability.
        fun cancelCast() =
            DisplayAbilityCastRange(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
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
