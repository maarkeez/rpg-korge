package com.mkz.rpg.lootDrop.domain

interface LootDropRepository {
    fun create(lootDrop: LootDrop)

    fun update(lootDrop: LootDrop)

    fun searchByUnitId(unitId: String): LootDrop?

    fun searchAll(): List<LootDrop>
}
