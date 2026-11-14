package com.mkz.tessavar.battlefield.adapters.storage

import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

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
