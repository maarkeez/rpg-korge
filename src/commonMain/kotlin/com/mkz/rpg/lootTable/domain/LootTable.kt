package com.mkz.rpg.lootTable.domain

import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto
import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto.TypeDto.LOOTABLE_ITEM
import com.mkz.rpg.lootTable.domain.LootTable.Dto.EntryDto.TypeDto.NOTHING
import com.mkz.rpg.lootTable.domain.LootTableError.DuplicatedLootTableEntry
import com.mkz.rpg.lootTable.domain.LootTableError.EmptyLootTableEntries
import com.mkz.rpg.lootTable.domain.LootTableError.InvalidLootTableEntryType
import com.mkz.rpg.lootTable.domain.LootTableError.NonPositiveLootTableWeight
import com.mkz.rpg.lootTable.domain.LootTableEvent.LootTableCreated
import kotlin.jvm.JvmInline

/** The weighted entries a defeated unit may drop. It only holds the weights: the loot drop turns them into chances. */
@ConsistentCopyVisibility
data class LootTable private constructor(
    private val id: Id,
    private val entries: Entries,
    private val events: Set<LootTableEvent>,
) {
    companion object {
        fun create(dto: Dto): LootTable =
            LootTable(
                id = Id(dto.id),
                entries = Entries.fromDtos(dto.entries),
                events = setOf(LootTableCreated(lootTableId = dto.id)),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            entries = entries.toDto(),
        )

    @JvmInline private value class Id(
        val value: String,
    )

    private data class Entries(
        val value: List<Entry>,
    ) {
        init {
            if (value.isEmpty()) throw EmptyLootTableEntries()
            if (value.size != value.map(Entry::lootableItemId).toSet().size) throw DuplicatedLootTableEntry()
        }

        companion object {
            fun fromDtos(dtos: List<EntryDto>): Entries = Entries(dtos.map(::Entry))
        }

        fun toDto(): List<EntryDto> = value.map(Entry::toDto)
    }

    /** A lootable item, or nothing when [lootableItemId] is `null`, dropped with the given [weight]. */
    private data class Entry(
        val lootableItemId: LootableItemId?,
        val weight: Weight,
    ) {
        constructor(dto: EntryDto) : this(
            lootableItemId =
                when (dto.type) {
                    LOOTABLE_ITEM -> LootableItemId(dto.lootableItemId ?: throw InvalidLootTableEntryType())
                    NOTHING -> null
                },
            weight = Weight(dto.weight),
        )

        fun toDto(): EntryDto =
            when (lootableItemId) {
                null -> EntryDto.nothing(weight = weight.value)
                else -> EntryDto.lootableItem(lootableItemId = lootableItemId.value, weight = weight.value)
            }
    }

    @JvmInline private value class LootableItemId(
        val value: String,
    )

    @JvmInline private value class Weight(
        val value: Int,
    ) {
        init {
            if (value <= 0) throw NonPositiveLootTableWeight()
        }
    }

    data class Dto(
        val id: String,
        val entries: List<EntryDto>,
    ) {
        data class EntryDto(
            val type: TypeDto,
            val weight: Int,
            val lootableItemId: String?,
        ) {
            companion object {
                fun lootableItem(
                    lootableItemId: String,
                    weight: Int,
                ) = EntryDto(type = LOOTABLE_ITEM, weight = weight, lootableItemId = lootableItemId)

                fun nothing(weight: Int) = EntryDto(type = NOTHING, weight = weight, lootableItemId = null)
            }

            enum class TypeDto {
                LOOTABLE_ITEM,
                NOTHING,
            }
        }
    }
}
