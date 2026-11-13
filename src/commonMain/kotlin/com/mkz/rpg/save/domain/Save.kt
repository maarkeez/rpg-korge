package com.mkz.rpg.save.domain

import com.mkz.rpg.save.domain.SaveError.IncompatibleSave
import com.mkz.rpg.save.domain.SaveError.SlotOutOfRange
import com.mkz.rpg.save.domain.SaveEvent.GameLoaded
import com.mkz.rpg.save.domain.SaveEvent.GameSaved
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Save private constructor(
    private val slot: Slot,
    private val version: Version,
    private val summary: Summary,
    private val snapshots: Snapshots,
    private val events: Set<SaveEvent>,
) {
    companion object {
        const val SAVE_VERSION = 10

        const val SLOTS = 3

        fun save(
            slot: Int,
            savedAt: Double,
            leaderUnitId: String?,
            partySize: Int,
            snapshots: Map<String, String>,
        ): Save =
            Save(
                slot = Slot(slot),
                version = Version(SAVE_VERSION),
                summary = Summary(savedAt = savedAt, leaderUnitId = leaderUnitId, partySize = partySize),
                snapshots = Snapshots(snapshots),
                events = setOf(GameSaved(slot = slot)),
            )

        fun read(dto: Dto): Save =
            Save(
                slot = Slot(dto.slot),
                version = Version(dto.summary.version),
                summary = Summary(savedAt = dto.summary.savedAt, leaderUnitId = dto.summary.leaderUnitId, partySize = dto.summary.partySize),
                snapshots = Snapshots(dto.snapshots),
                events = emptySet(),
            )

        fun requireSlot(slot: Int) {
            Slot(slot)
        }

        fun slotOf(
            slot: Int,
            save: Save?,
        ): Dto.SlotDto =
            when {
                save == null -> Dto.SlotDto(slot = slot, state = Dto.SlotStateDto.EMPTY, summary = null)
                save.version.value != SAVE_VERSION -> Dto.SlotDto(slot = slot, state = Dto.SlotStateDto.INCOMPATIBLE, summary = null)
                else -> Dto.SlotDto(slot = slot, state = Dto.SlotStateDto.SAVED, summary = save.summary.toDto(save.version))
            }
    }

    fun toDto() =
        Dto(
            slot = slot.value,
            summary = summary.toDto(version),
            snapshots = snapshots.value,
        )

    fun load(): Pair<Map<String, String>, Save> {
        if (version.value != SAVE_VERSION) throw IncompatibleSave(slot.value)
        return snapshots.value to copy(events = events + GameLoaded(slot = slot.value))
    }

    fun pullEvents(): Pair<Set<SaveEvent>, Save> = events to copy(events = emptySet())

    @JvmInline private value class Slot(
        val value: Int,
    ) {
        init {
            if (value !in 1..SLOTS) throw SlotOutOfRange()
        }
    }

    @JvmInline private value class Version(
        val value: Int,
    )

    private data class Summary(
        val savedAt: Double,
        val leaderUnitId: String?,
        val partySize: Int,
    ) {
        fun toDto(version: Version) = Dto.SummaryDto(version = version.value, savedAt = savedAt, leaderUnitId = leaderUnitId, partySize = partySize)
    }

    @JvmInline private value class Snapshots(
        val value: Map<String, String>,
    )

    data class Dto(
        val slot: Int,
        val summary: SummaryDto,
        val snapshots: Map<String, String>,
    ) {
        data class SummaryDto(
            val version: Int,
            val savedAt: Double,
            val leaderUnitId: String?,
            val partySize: Int,
        )

        data class SlotDto(
            val slot: Int,
            val state: SlotStateDto,
            val summary: SummaryDto?,
        )

        enum class SlotStateDto {
            EMPTY,
            SAVED,

            INCOMPATIBLE,
        }
    }
}
