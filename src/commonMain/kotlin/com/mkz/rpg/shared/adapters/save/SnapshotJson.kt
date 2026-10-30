package com.mkz.rpg.shared.adapters.save

import kotlinx.serialization.json.Json

/**
 * The JSON every snapshot of a saved game is written with. A key it does not know fails to decode, so a save of another
 * shape is incompatible instead of loading half of it.
 */
val SnapshotJson =
    Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
    }
