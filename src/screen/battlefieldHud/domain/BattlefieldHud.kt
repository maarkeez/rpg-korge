package screen.battlefieldHud.domain

import screen.battlefieldHud.domain.BattlefieldHudEvent.SelectedBattleUnit

sealed interface BattlefieldHud {
    object Idle : BattlefieldHud {
        fun selectBattleUnit(
            tile: Dto.TileDto,
            battleUnitId: String,
            tilesWhereCanBeMoved: Set<Dto.TileDto>
        )= DisplayMovementRange(
            tile = tile,
            battleUnitId = battleUnitId,
            tilesWhereCanBeMoved = tilesWhereCanBeMoved,
            events = setOf(
                SelectedBattleUnit(
                    tile = tile,
                    battleUnitId = battleUnitId,
                    tilesWhereCanBeMoved = tilesWhereCanBeMoved
                )
            )
        )
    }

    data class DisplayMovementRange(
        val tile: Dto.TileDto,
        val battleUnitId: String,
        val tilesWhereCanBeMoved: Set<Dto.TileDto>,
        private val events: Set<BattlefieldHudEvent>,
    ): BattlefieldHud{

        fun pullEvents() = events to copy(events = emptySet())
    }

    interface Dto {
        data class TileDto(val row: Int, val column: Int)
    }
}
