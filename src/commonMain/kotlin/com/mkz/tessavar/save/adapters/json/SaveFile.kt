package com.mkz.tessavar.save.adapters.json

import com.mkz.tessavar.save.domain.Save
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

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
