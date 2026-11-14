package com.mkz.tessavar.battlefield.adapters.resources

import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader.BattlefieldDefinition
import com.mkz.tessavar.battlefield.domain.BattlefieldError.InvalidBattlefieldFormat
import com.mkz.tessavar.battlefield.usecases.services.BattlefieldDecoder
import com.mkz.tessavar.shared.adapters.toml.TomlService

class TomlBattlefieldDecoder : BattlefieldDecoder {
    override fun decode(text: String): List<List<String>> =
        try {
            TomlService().deserialize<BattlefieldDefinition>(text).toTiles()
        } catch (exception: Exception) {
            throw InvalidBattlefieldFormat(exception.message)
        }
}
