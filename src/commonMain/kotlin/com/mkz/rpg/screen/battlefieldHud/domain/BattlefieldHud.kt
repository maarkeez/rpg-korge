package com.mkz.rpg.screen.battlefieldHud.domain

import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.AbilityCastPreviewed
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.AbilityDeselected
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnit
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnitAbility

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

        fun previewAbilityCast(castGroup: Dto.CastGroupDto) =
            DisplayAbilityCastPreview(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved,
                castGroupsWhereCanCast = castGroupsWhereCanCast,
                castGroup = castGroup,
                events =
                    events +
                        AbilityCastPreviewed(
                            casterBattleUnitId = battleUnitId,
                            abilityId = abilityId,
                            castGroup = castGroup,
                        ),
            )

        fun idle() = Idle(events + BattlefieldHudEvent.Idle)
    }

    data class DisplayAbilityCastPreview(
        val casterTile: Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val tilesWhereCanBeMoved: Set<Dto.TileDto>,
        val castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        val castGroup: Dto.CastGroupDto,
        private val events: Set<BattlefieldHudEvent>,
    ) : BattlefieldHud {
        fun pullEvents() = events to copy(events = emptySet())

        fun idle() = Idle(events + BattlefieldHudEvent.Idle)

        /** Switches the preview to another valid cast group. */
        fun previewAbilityCast(newCastGroup: Dto.CastGroupDto): DisplayAbilityCastPreview =
            copy(
                castGroup = newCastGroup,
                events =
                    events +
                        AbilityCastPreviewed(
                            casterBattleUnitId = battleUnitId,
                            abilityId = abilityId,
                            castGroup = newCastGroup,
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
