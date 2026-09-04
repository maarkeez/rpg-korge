package screen.battlefieldHud.usecases.commands

import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.Idle

class InitializeBattlefieldHud(
    private val battlefieldHudRepository: BattlefieldHudRepository
) {
    operator fun invoke() {
        battlefieldHudRepository.create(Idle)
    }
}
