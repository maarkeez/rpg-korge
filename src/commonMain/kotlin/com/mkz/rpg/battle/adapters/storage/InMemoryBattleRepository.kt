package com.mkz.rpg.battle.adapters.storage

import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battle.domain.BattleRepository

// Holds the battle in progress, or the last one finished, which the next battle replaces.
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
