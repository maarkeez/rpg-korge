package com.mkz.rpg.lootDrop.domain

interface LootDropRepository {
    fun create(lootDrop: LootDrop)

    fun update(lootDrop: LootDrop)

    fun searchByUnitId(unitId: String): LootDrop?

    /** Every loot drop, in the order they were created. */
    fun searchAll(): List<LootDrop>
}
