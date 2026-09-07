package battle.adapters.storage

import battle.domain.*

class InMemoryBattleRepository: BattleRepository {
    private var battle : Battle? = null
    override fun create(battle: Battle) {
        this.battle = battle
    }

    override fun update(battle: Battle) {
        this.battle = battle
    }

    override fun search() = battle
}
