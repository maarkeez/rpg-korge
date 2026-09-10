package com.mkz.rpg.unit.domain

interface UnitRepository {
    fun create(unit: Unit)

    fun searchById(id: String): Unit?
}
