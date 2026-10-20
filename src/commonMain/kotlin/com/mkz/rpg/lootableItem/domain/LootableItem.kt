package com.mkz.rpg.lootableItem.domain

import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.ConsumableDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.EquippableDto.EquipmentZoneDto
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.CONSUMABLE
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.TypeDto.EQUIPPABLE
import com.mkz.rpg.lootableItem.domain.LootableItemError.InvalidLootType
import com.mkz.rpg.lootableItem.domain.LootableItemError.NegativePrice
import com.mkz.rpg.lootableItem.domain.LootableItemError.PriceAboveLimit
import com.mkz.rpg.lootableItem.domain.LootableItemEvent.LootableItemCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class LootableItem private constructor(
    private val id: Id,
    private val name: Name,
    private val price: Price,
    private val lootType: LootType,
    private val events: Set<LootableItemEvent>,
) {
    companion object {
        fun create(dto: Dto): LootableItem =
            LootableItem(
                id = Id(dto.id),
                name = Name(dto.name),
                price = Price(dto.price),
                lootType = LootType(dto.lootType),
                events = setOf(LootableItemCreated(lootableItemId = dto.id)),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            name = name.value,
            price = price.value,
            lootType = lootType.toDto(),
        )

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class Name(
        val value: String,
    )

    // In gold.
    @JvmInline private value class Price(
        val value: Int,
    ) {
        init {
            if (value < 0) throw NegativePrice()
            if (value > 999) throw PriceAboveLimit()
        }
    }

    sealed interface LootType {
        fun toDto(): LootTypeDto

        companion object {
            operator fun invoke(dto: LootTypeDto): LootType =
                when (dto.type) {
                    CONSUMABLE -> Consumable(dto.consumable ?: throw InvalidLootType())
                    EQUIPPABLE -> Equippable(dto.equippable ?: throw InvalidLootType())
                }
        }

        @ConsistentCopyVisibility
        private data class Consumable private constructor(
            private val effectIds: EffectIds,
        ) : LootType {
            companion object {
                operator fun invoke(dto: ConsumableDto) = Consumable(EffectIds(dto.effectIds))
            }

            override fun toDto() =
                LootTypeDto.consumable(
                    consumable = ConsumableDto(effectIds = effectIds.toDto()),
                )
        }

        @ConsistentCopyVisibility
        private data class Equippable private constructor(
            private val effectIds: EffectIds,
            private val equipmentZone: EquipmentZone,
        ) : LootType {
            companion object {
                operator fun invoke(dto: EquippableDto) =
                    Equippable(
                        effectIds = EffectIds(dto.effectIds),
                        equipmentZone = EquipmentZone(dto.equipmentZone),
                    )
            }

            override fun toDto() =
                LootTypeDto.equippable(
                    equippable =
                        EquippableDto(
                            effectIds = effectIds.toDto(),
                            equipmentZone = equipmentZone.toDto(),
                        ),
                )

            private enum class EquipmentZone {
                HEAD,
                BODY,
                ARMS,
                LEGS,
                WEAPON,
                ;

                companion object {
                    operator fun invoke(dto: EquipmentZoneDto) =
                        when (dto) {
                            EquipmentZoneDto.HEAD -> HEAD
                            EquipmentZoneDto.BODY -> BODY
                            EquipmentZoneDto.ARMS -> ARMS
                            EquipmentZoneDto.LEGS -> LEGS
                            EquipmentZoneDto.WEAPON -> WEAPON
                        }
                }

                fun toDto() =
                    when (this) {
                        HEAD -> EquipmentZoneDto.HEAD
                        BODY -> EquipmentZoneDto.BODY
                        ARMS -> EquipmentZoneDto.ARMS
                        LEGS -> EquipmentZoneDto.LEGS
                        WEAPON -> EquipmentZoneDto.WEAPON
                    }
            }
        }

        @ConsistentCopyVisibility
        private data class EffectIds private constructor(
            private val effectIds: Set<EffectId>,
        ) {
            fun toDto(): Set<String> = effectIds.map(EffectId::value).toSet()

            companion object {
                operator fun invoke(effectIds: Set<String>) = EffectIds(effectIds.map(::EffectId).toSet())
            }

            @JvmInline private value class EffectId(
                val value: String,
            )
        }
    }

    data class Dto(
        val id: String,
        val name: String,
        val price: Int,
        val lootType: LootTypeDto,
    ) {
        /** What a merchant pays for it: half its price, rounded down. */
        val sellPrice: Int get() = price / 2

        data class LootTypeDto(
            val type: TypeDto,
            val consumable: ConsumableDto?,
            val equippable: EquippableDto?,
        ) {
            companion object {
                fun consumable(consumable: ConsumableDto) =
                    LootTypeDto(
                        type = CONSUMABLE,
                        consumable = consumable,
                        equippable = null,
                    )

                fun equippable(equippable: EquippableDto) =
                    LootTypeDto(
                        type = EQUIPPABLE,
                        consumable = null,
                        equippable = equippable,
                    )
            }

            enum class TypeDto {
                CONSUMABLE,
                EQUIPPABLE,
            }

            data class ConsumableDto(
                val effectIds: Set<String>,
            )

            data class EquippableDto(
                val effectIds: Set<String>,
                val equipmentZone: EquipmentZoneDto,
            ) {
                enum class EquipmentZoneDto {
                    HEAD,
                    BODY,
                    ARMS,
                    LEGS,
                    WEAPON,
                }
            }
        }
    }
}
