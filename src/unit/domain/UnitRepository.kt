package unit.domain

interface UnitRepository {
    fun create(unit:Unit)
    fun searchById(id: String): Unit?
}
