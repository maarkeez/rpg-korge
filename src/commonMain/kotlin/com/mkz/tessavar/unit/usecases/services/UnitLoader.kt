package com.mkz.tessavar.unit.usecases.services

import com.mkz.tessavar.unit.domain.Unit

interface UnitLoader {
    suspend fun initResources() {}

    fun loadUnits(): List<Unit.Dto>
}
