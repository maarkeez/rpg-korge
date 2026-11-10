package com.mkz.rpg.unit.usecases.services

import com.mkz.rpg.unit.domain.Unit

class FakeUnitLoader(
    private val units: List<Unit.Dto> = emptyList(),
) : UnitLoader {
    override fun loadUnits(): List<Unit.Dto> = units
}
