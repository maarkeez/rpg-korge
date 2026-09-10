package com.mkz.rpg.battlefield.adapters.storage

import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository

class InMemoryBattlefieldRepository : BattlefieldRepository {
    private var battlefield: Battlefield? = null

    override fun create(battlefield: Battlefield) {
        this.battlefield = battlefield
    }

    override fun update(battlefield: Battlefield) {
        this.battlefield = battlefield
    }

    override fun search() = battlefield
}
