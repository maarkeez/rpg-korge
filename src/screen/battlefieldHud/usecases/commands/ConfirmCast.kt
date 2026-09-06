package screen.battlefieldHud.usecases.commands

import battleunit.adapters.presentation.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import shared.domain.*

class ConfirmCast(
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is DisplayAbilityCastPreview -> {
                battleUnitApi.castAbility(
                    battleUnitId= battlefieldHud.battleUnitId,
                    abilityId = battlefieldHud.abilityId,
                    row = battlefieldHud.castTile.row,
                    column = battlefieldHud.castTile.column,
                )
                val (events, updatedBattlefieldHud) = battlefieldHud.idle().pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }

            is DisplayAbilityCastRange,
            is DisplayMovementRange,
            is Idle -> throw BattlefieldHudError.InvalidBattlefieldHudState()
        }
    }
}
