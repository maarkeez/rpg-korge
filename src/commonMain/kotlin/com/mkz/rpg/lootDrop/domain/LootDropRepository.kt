package com.mkz.rpg.lootDrop.domain

interface LootDropRepository {
    fun create(lootDrop: LootDrop)

    fun searchByUnitId(unitId: String): LootDrop?
}
