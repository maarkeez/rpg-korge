package com.mkz.rpg.telemetry.adapters.save

import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import com.mkz.rpg.telemetry.usecases.commands.LoadTelemetrySnapshot
import com.mkz.rpg.telemetry.usecases.queries.SearchTelemetrySnapshot
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class TelemetrySnapshottable(
    private val searchTelemetrySnapshot: SearchTelemetrySnapshot,
    private val loadTelemetrySnapshot: LoadTelemetrySnapshot,
) : Snapshottable {
    override val key = "telemetry"

    override fun snapshot(): String = SnapshotJson.encodeToString(TelemetrySnapshot(gameId = searchTelemetrySnapshot()))

    override fun load(snapshot: String) = loadTelemetrySnapshot(SnapshotJson.decodeFromString<TelemetrySnapshot>(snapshot).gameId)
}

@Serializable
private data class TelemetrySnapshot(
    val gameId: String?,
)
