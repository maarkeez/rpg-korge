package com.mkz.rpg.save.domain

import com.mkz.rpg.save.domain.SaveError.IncompatibleSave
import com.mkz.rpg.save.domain.SaveError.SlotOutOfRange
import com.mkz.rpg.save.domain.SaveEvent.GameRestored
import com.mkz.rpg.save.domain.SaveEvent.GameSaved
import kotlin.jvm.JvmInline

/**
 * A game saved into one of the [SLOTS] save slots: the snapshot of each subdomain holding runtime state, keyed by its
 * name (e.g.: `battleUnit`), and the summary the slot shows: when it was saved, the leader of the party and its size.
 *
 * It is saved with [SAVE_VERSION]. A save of another version can not be loaded, as the snapshots may have another shape:
 * there are no migrations.
 */
@ConsistentCopyVisibility
data class Save private constructor(
    private val slot: Slot,
    private val version: Version,
    private val summary: Summary,
    private val snapshots: Snapshots,
    private val events: Set<SaveEvent>,
) {
    companion object {
        /** The version of the saves written by this game. Bump it whenever a snapshot changes its shape. */
        const val SAVE_VERSION = 1

        const val SLOTS = 3

        /** Saves the game into the slot, replacing the game saved there, if any. Saved at: in milliseconds since the epoch. */
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

        /** The game as it was saved, read from the slot. */
        fun read(dto: Dto): Save =
            Save(
                slot = Slot(dto.slot),
                version = Version(dto.summary.version),
                summary = Summary(savedAt = dto.summary.savedAt, leaderUnitId = dto.summary.leaderUnitId, partySize = dto.summary.partySize),
                snapshots = Snapshots(dto.snapshots),
                events = emptySet(),
            )

        /** Fails when the slot is not one of the [SLOTS] save slots. */
        fun requireSlot(slot: Int) {
            Slot(slot)
        }

        /** What a slot shows, from its summary, or `null` when it is empty. */
        fun slotOf(
            slot: Int,
            summary: Dto.SummaryDto?,
        ): Dto.SlotDto =
            when {
                summary == null -> Dto.SlotDto(slot = slot, state = Dto.SlotStateDto.EMPTY, summary = null)
                summary.version != SAVE_VERSION -> Dto.SlotDto(slot = slot, state = Dto.SlotStateDto.INCOMPATIBLE, summary = null)
                else -> Dto.SlotDto(slot = slot, state = Dto.SlotStateDto.SAVED, summary = summary)
            }
    }

    fun toDto() =
        Dto(
            slot = slot.value,
            summary = summary.toDto(version),
            snapshots = snapshots.value,
        )

    /**
     * The snapshot of each subdomain, to restore them, then announces the game is restored. Fails with
     * [SaveError.IncompatibleSave] when it was saved with another version.
     */
    fun load(): Pair<Map<String, String>, Save> {
        if (version.value != SAVE_VERSION) throw IncompatibleSave(slot.value)
        return snapshots.value to copy(events = events + GameRestored(slot = slot.value))
    }

    fun pullEvents() = events to copy(events = emptySet())

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

    /** Snapshots: the snapshot of each subdomain, by its name, e.g.: `battleUnit`. */
    data class Dto(
        val slot: Int,
        val summary: SummaryDto,
        val snapshots: Map<String, String>,
    ) {
        /**
         * What a slot shows. Saved at: in milliseconds since the epoch. Leader: the unit of the party leader, or `null`
         * when every member was defeated.
         */
        data class SummaryDto(
            val version: Int,
            val savedAt: Double,
            val leaderUnitId: String?,
            val partySize: Int,
        )

        /** A save slot, from 1 to [SLOTS]. Summary: only when a game of this version is saved in it. */
        data class SlotDto(
            val slot: Int,
            val state: SlotStateDto,
            val summary: SummaryDto?,
        )

        enum class SlotStateDto {
            EMPTY,
            SAVED,

            // Saved by another version of the game, or unreadable: it can only be deleted.
            INCOMPATIBLE,
        }
    }
}
