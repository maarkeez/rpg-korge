package com.mkz.rpg.shared.adapters.save

import kotlinx.serialization.json.Json

val SnapshotJson =
    Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
    }
