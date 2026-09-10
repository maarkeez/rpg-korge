package com.mkz.rpg.screen.battlefieldHud.adapters.storage

import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository

class InMemoryBattlefieldHudRepository : BattlefieldHudRepository {
    private var battlefieldHud: BattlefieldHud? = null

    override fun create(battlefieldHud: BattlefieldHud) {
        this.battlefieldHud = battlefieldHud
    }

    override fun update(battlefieldHud: BattlefieldHud) {
        this.battlefieldHud = battlefieldHud
    }

    override fun search(): BattlefieldHud? = battlefieldHud
}
