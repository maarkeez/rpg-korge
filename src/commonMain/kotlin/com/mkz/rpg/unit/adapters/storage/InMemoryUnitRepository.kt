package com.mkz.rpg.unit.adapters.storage

import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitRepository

class InMemoryUnitRepository : UnitRepository {
    private val units = mutableMapOf<String, Unit>()

    override fun create(unit: Unit) {
        units[unit.toDto().id] = unit
    }

    override fun searchById(id: String) = units[id]
}
