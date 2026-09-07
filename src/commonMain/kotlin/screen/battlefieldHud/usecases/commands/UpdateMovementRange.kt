package screen.battlefieldHud.usecases.commands

import battlefield.adapters.presentation.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.*

class UpdateMovementRange(
    private val battlefieldApi: BattlefieldApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
    private val movementService: MovementService,
) {
    operator fun invoke(battleUnitId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is DisplayMovementRange -> {
                if(battleUnitId != battlefieldHud.battleUnitId) return
                val position = battlefieldApi.searchPosition(battleUnitId)!!
                val (events, updatedBattlefieldHud) = battlefieldHud
                    .tilesWhereCanBeMoved(
                        tile = TileDto(position.row, position.column),
                        tilesWhereCanBeMoved = movementService.tilesWhereCanMove(battleUnitId)
                    )
                    .pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
            is Idle,
            is DisplayAbilityCastRange,
            is DisplayAbilityCastPreview  -> return
        }
    }
}
