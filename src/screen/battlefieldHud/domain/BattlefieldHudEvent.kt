package screen.battlefieldHud.domain

import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import shared.domain.DomainEvent

sealed interface BattlefieldHudEvent: DomainEvent {

    data class SelectedBattleUnit(
        val tile: TileDto,
        val battleUnitId: String,
        val tilesWhereCanBeMoved: Set<TileDto>
    ): BattlefieldHudEvent
}
