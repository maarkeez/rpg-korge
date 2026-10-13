package com.mkz.rpg.lootableItem.domain

import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.ConsumableDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import korlibs.io.util.UUID

object LootableItemMother {
    fun lootableItem(
        id: String = lootableItemId(),
        name: String = lootableItemName(),
        lootType: LootTypeDto = arrayOf(LootTypeDto.consumable(consumable()), LootTypeDto.equippable(equippable())).random(),
    ): LootableItem =
        LootableItem
            .create(dto = LootableItem.Dto(id = id, name = name, lootType = lootType))
            .pullEvents()
            .second

    fun consumableLootableItem(
        id: String = lootableItemId(),
        name: String = lootableItemName(),
        consumable: ConsumableDto = consumable(),
    ): LootableItem =
        LootableItem
            .create(
                dto =
                    LootableItem.Dto(
                        id = id,
                        name = name,
                        lootType =
                            LootTypeDto(
                                type = LootTypeDto.TypeDto.CONSUMABLE,
                                consumable = consumable,
                                equippable = null,
                            ),
                    ),
            ).pullEvents()
            .second

    fun equippableLootableItem(
        id: String = lootableItemId(),
        name: String = lootableItemName(),
        equippable: EquippableDto = equippable(),
    ): LootableItem =
        LootableItem
            .create(
                dto =
                    LootableItem.Dto(
                        id = id,
                        name = name,
                        lootType =
                            LootTypeDto(
                                type = LootTypeDto.TypeDto.EQUIPPABLE,
                                consumable = null,
                                equippable = equippable,
                            ),
                    ),
            ).pullEvents()
            .second

    fun consumable(effectIds: Set<String> = setOf(effectId())): ConsumableDto = ConsumableDto(effectIds = effectIds)

    fun equippable(
        effectIds: Set<String> = setOf(effectId()),
        equipmentZone: EquipmentZoneDto = EquipmentZoneDto.entries.random(),
    ): EquippableDto = EquippableDto(effectIds = effectIds, equipmentZone = equipmentZone)

    fun lootableItemId(): String = "lootable-item-" + UUID.randomUUID().toString()

    fun lootableItemName(): String = "lootable-item-name-" + UUID.randomUUID().toString()

    fun effectId(): String = "lootable-item-efect-id" + UUID.randomUUID().toString()
}
