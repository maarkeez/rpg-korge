package screen.battlefieldHud.usecases.commands

import battlefield.usecases.queries.SearchPosition
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import screen.battlefieldHud.domain.BattlefieldHudRepository
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.EventBus

class UpdateMovementRange(
    private val searchPosition: SearchPosition,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
    private val movementService: MovementService,
) {
    operator fun invoke(battleUnitId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when (battlefieldHud) {
            is DisplayMovementRange -> {
                if (battleUnitId != battlefieldHud.battleUnitId) return
                val position = searchPosition(battleUnitId)!!
                val (events, updatedBattlefieldHud) =
                    battlefieldHud
                        .tilesWhereCanBeMoved(
                            tile = TileDto(position.row, position.column),
                            tilesWhereCanBeMoved = movementService.tilesWhereCanMove(battleUnitId),
                        ).pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
            is Idle,
            is DisplayAbilityCastRange,
            is DisplayAbilityCastPreview,
            -> return
        }
    }
}
