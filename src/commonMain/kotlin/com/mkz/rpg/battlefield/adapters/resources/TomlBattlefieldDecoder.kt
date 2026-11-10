package com.mkz.rpg.battlefield.adapters.resources

import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader.BattlefieldDefinition
import com.mkz.rpg.battlefield.domain.BattlefieldError.InvalidBattlefieldFormat
import com.mkz.rpg.battlefield.usecases.services.BattlefieldDecoder
import com.mkz.rpg.shared.adapters.toml.TomlService

class TomlBattlefieldDecoder : BattlefieldDecoder {
    override fun decode(text: String): List<List<String>> =
        try {
            TomlService().deserialize<BattlefieldDefinition>(text).toTiles()
        } catch (exception: Exception) {
            throw InvalidBattlefieldFormat(exception.message)
        }
}
