package screen.battlefieldHud.domain

import screen.battlefieldHud.domain.BattlefieldHudEvent.AbilityDeselected
import screen.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnit
import screen.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnitAbility
import screen.battlefieldHud.domain.BattlefieldHudEvent.SelfAbilityCastPreviewed
import kotlin.collections.plus

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
            tilesWhereCanBeMoved: Set<Dto.TileDto>
        )= DisplayMovementRange(
            tile = tile,
            battleUnitId = battleUnitId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            events = events + SelectedBattleUnit(
                    tile = tile,
                    battleUnitId = battleUnitId,
                    tilesWhereCanBeMoved = tilesWhereCanBeMoved
            )
        )

        fun pullEvents() = events to copy(events = emptySet())
    }

    data class DisplayMovementRange(
        val tile: Dto.TileDto,
        val battleUnitId: String,
        val tilesWhereCanBeMoved: Set<Dto.TileDto>,
        private val events: Set<BattlefieldHudEvent>,
    ): BattlefieldHud {

        fun pullEvents() = events to copy(events = emptySet())
        fun idle() = Idle(events + BattlefieldHudEvent.Idle)
        fun selectAbility(abilityId: String, tilesWhereCanCast: Set<Dto.TileDto>) = DisplayAbilityCastRange(
            casterTile = tile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            tilesWhereCanCast = tilesWhereCanCast,
            events = events + SelectedBattleUnitAbility(
                casterTile = tile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                tilesWhereCanCast = tilesWhereCanCast
            )
        )

        fun tilesWhereCanBeMoved(
            tile: Dto.TileDto,
            tilesWhereCanBeMoved: Set<Dto.TileDto>
        ) = copy(
            tile = tile,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            events = events
                + SelectedBattleUnit(
                    tile = tile,
                    battleUnitId = battleUnitId,
                    tilesWhereCanBeMoved = tilesWhereCanBeMoved
                )
        )
    }

    data class DisplayAbilityCastRange(
        val casterTile: Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val tilesWhereCanBeMoved: Set<Dto.TileDto>,
        val tilesWhereCanCast: Set<Dto.TileDto>,
        private val events: Set<BattlefieldHudEvent>,
    ): BattlefieldHud {
        fun pullEvents() = events to copy(events = emptySet())
        fun deselectAbility() = DisplayMovementRange(
            tile = casterTile,
            battleUnitId = battleUnitId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            events = events
                + AbilityDeselected(abilityId)
                + SelectedBattleUnit(
                tile = casterTile,
                battleUnitId = battleUnitId,
                tilesWhereCanBeMoved = tilesWhereCanBeMoved
            )
        )

        fun previewSelfAbilityCast(
            castTile: Dto.TileDto,
        ) = DisplayAbilityCastPreview(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            tilesWhereCanCast = tilesWhereCanCast,
            castTile = castTile,
            enemyBattleUnitId = null,
            events = events
                + SelfAbilityCastPreviewed(
                    casterBattleUnitId = battleUnitId,
                    abilityId = abilityId,
                    castTile = castTile
                )
        )

        fun idle() = Idle(events + BattlefieldHudEvent.Idle)

        fun previewEnemyAbilityCast(castTile: Dto.TileDto, enemyBattleUnitId: String) = DisplayAbilityCastPreview(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            tilesWhereCanCast = tilesWhereCanCast,
            castTile = castTile,
            enemyBattleUnitId = enemyBattleUnitId,
            events = events
                + BattlefieldHudEvent.EnemyAbilityCastPreviewed(
                    casterBattleUnitId = battleUnitId,
                    abilityId = abilityId,
                    castTile = castTile,
                    enemyBattleUnitId = enemyBattleUnitId
                )
        )
    }

    data class DisplayAbilityCastPreview(
        val casterTile: BattlefieldHud.Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val tilesWhereCanBeMoved: Set<BattlefieldHud.Dto.TileDto>,
        val tilesWhereCanCast: Set<BattlefieldHud.Dto.TileDto>,
        val castTile: Dto.TileDto,
        val enemyBattleUnitId: String?,
        private val events: Set<BattlefieldHudEvent>,
    ): BattlefieldHud {
        fun pullEvents() = events to copy(events = emptySet())
        fun idle() = Idle(events + BattlefieldHudEvent.Idle)
    }

    interface Dto {
        data class TileDto(val row: Int, val column: Int)
    }
}
