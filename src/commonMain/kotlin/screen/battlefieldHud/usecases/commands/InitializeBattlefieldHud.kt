package screen.battlefieldHud.usecases.commands

import screen.battlefieldHud.domain.BattlefieldHud.Idle
import screen.battlefieldHud.domain.BattlefieldHudRepository

class InitializeBattlefieldHud(
    private val battlefieldHudRepository: BattlefieldHudRepository,
) {
    operator fun invoke() {
        battlefieldHudRepository.create(Idle.create())
    }
}
