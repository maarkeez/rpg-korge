package screen.battlefieldHud.adapters.storage

import screen.battlefieldHud.domain.BattlefieldHud
import screen.battlefieldHud.domain.BattlefieldHudRepository

class InMemoryBattlefieldHudRepositoryImpl : BattlefieldHudRepository {

    private var battlefieldHud: BattlefieldHud? = null

    override fun create(battlefieldHud: BattlefieldHud) {
        this.battlefieldHud = battlefieldHud
    }

    override fun update(battlefieldHud: BattlefieldHud) {
        this.battlefieldHud = battlefieldHud
    }

    override fun search(): BattlefieldHud? = battlefieldHud
}
