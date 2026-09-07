package unit.adapters.storage

import unit.domain.Unit
import unit.domain.UnitRepository

class InMemoryUnitRepository : UnitRepository {
    private val units = mutableMapOf<String, Unit>()

    override fun create(unit: Unit) {
        units[unit.toDto().id] = unit
    }

    override fun searchById(id: String) = units[id]
}
