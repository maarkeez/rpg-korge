package com.mkz.tessavar.battle.adapters.storage

import com.mkz.tessavar.battle.domain.Battle
import com.mkz.tessavar.battle.domain.BattleRepository

class InMemoryBattleRepository : BattleRepository {
    private var battle: Battle? = null

    override fun create(battle: Battle) {
        this.battle = battle
    }

    override fun update(battle: Battle) {
        this.battle = battle
    }

    override fun search() = battle
}
