package com.mkz.rpg.shared.adapters.toml

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.decodeFromString

class TomlService {
    inline fun <reified T> deserialize(toml: String): T = Toml.Default.decodeFromString(toml)
}
