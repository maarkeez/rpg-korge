package com.mkz.tessavar.unit.usecases.services

import com.mkz.tessavar.unit.domain.Unit

class FakeUnitLoader(
    private val units: List<Unit.Dto> = emptyList(),
) : UnitLoader {
    override fun loadUnits(): List<Unit.Dto> = units
}
