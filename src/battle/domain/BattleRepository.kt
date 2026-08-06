package battle.domain

import battlefield.domain.Battlefield

interface BattleRepository {
    fun create(battle: Battle)
    fun update(battle: Battle)
    fun search(): Battle?
}
