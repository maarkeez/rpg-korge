package com.mkz.rpg.unit.usecases.services

import com.mkz.rpg.unit.domain.Unit

interface UnitLoader {
    suspend fun initResources() {}

    fun loadUnits(): List<Unit.Dto>
}
