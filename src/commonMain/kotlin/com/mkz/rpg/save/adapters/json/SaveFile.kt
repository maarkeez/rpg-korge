package com.mkz.rpg.save.adapters.json

import com.mkz.rpg.save.domain.Save
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * A saved game as it is stored: its summary, and the snapshot of each subdomain, kept as the JSON it wrote, so the file
 * reads as one document. A key it does not know fails to decode, so a save of another shape is incompatible.
 */
@Serializable
data class SaveFile(
    val summary: SaveSummaryFile,
    val snapshots: Map<String, JsonElement>,
) {
    companion object {
        val json =
            Json {
                ignoreUnknownKeys = false
                prettyPrint = false
            }

        fun from(dto: Save.Dto) = SaveFile(summary = SaveSummaryFile.from(dto.summary), snapshots = dto.snapshots.mapValues { (_, snapshot) -> json.parseToJsonElement(snapshot) })
    }

    fun toDto(slot: Int) = Save.Dto(slot = slot, summary = summary.toDto(), snapshots = snapshots.mapValues { (_, snapshot) -> snapshot.toString() })
}

/** What a slot shows, stored apart too, so listing the slots does not read their snapshots. Saved at: in milliseconds since the epoch. */
@Serializable
data class SaveSummaryFile(
    val version: Int,
    val savedAt: Double,
    val leaderUnitId: String?,
    val partySize: Int,
) {
    companion object {
        fun from(dto: Save.Dto.SummaryDto) = SaveSummaryFile(version = dto.version, savedAt = dto.savedAt, leaderUnitId = dto.leaderUnitId, partySize = dto.partySize)
    }

    fun toDto() = Save.Dto.SummaryDto(version = version, savedAt = savedAt, leaderUnitId = leaderUnitId, partySize = partySize)
}
