package battleunit.domain

interface BattleUnitRepository {
    fun create(battleUnit: BattleUnit)
    fun update(battleUnit: BattleUnit)
    fun searchById(id: String): BattleUnit?
    fun searchByPlayerId(playerId: String): List<BattleUnit>
    fun searchAll(): List<BattleUnit>
}
