package screen.battlefieldHud.usecases.commands

import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import shared.domain.*

class CancelCast(
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is DisplayAbilityCastPreview -> {
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
