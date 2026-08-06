package battleunit.domain

interface BattleUnitRepository {
    fun create(battleUnit: BattleUnit)
    fun searchById(id: String): BattleUnit?
}
