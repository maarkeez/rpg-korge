package com.mkz.tessavar.unit.adapters.storage

import com.mkz.tessavar.unit.domain.Unit
import com.mkz.tessavar.unit.domain.UnitRepository

class InMemoryUnitRepository : UnitRepository {
    private val units = mutableMapOf<String, Unit>()

    override fun create(unit: Unit) {
        units[unit.toDto().id] = unit
    }

    override fun searchById(id: String) = units[id]
}
