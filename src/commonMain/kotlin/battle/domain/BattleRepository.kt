package battle.domain

interface BattleRepository {
    fun create(battle: Battle)
    fun update(battle: Battle)
    fun search(): Battle?
}
