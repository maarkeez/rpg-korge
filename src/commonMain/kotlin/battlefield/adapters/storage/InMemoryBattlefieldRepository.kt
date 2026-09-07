package battlefield.adapters.storage

import battlefield.domain.Battlefield
import battlefield.domain.BattlefieldRepository

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
