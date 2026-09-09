package screen.battlefieldHud.usecases.commands

import battleunit.adapters.presentation.BattleUnitApi
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudError
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import screen.battlefieldHud.domain.BattlefieldHudRepository
import shared.domain.EventBus

class ConfirmCast(
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when (battlefieldHud) {
            is DisplayAbilityCastPreview -> {
                battleUnitApi.castAbility(
                    battleUnitId = battlefieldHud.battleUnitId,
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
            is Idle,
            -> throw BattlefieldHudError.InvalidBattlefieldHudState()
        }
    }
}
