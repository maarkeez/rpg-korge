package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class ExportBattlefieldTest {
    private val exportBattlefield = ExportBattlefield(TomlBattlefieldEncoder())

    @Test
    fun `should write the terrain id of each tile when the battlefield is exported`() {
        // Given
        val tiles = listOf(listOf("sand", "lava"))
        // When
        val exported = exportBattlefield(tiles)
        // Then
        exported shouldContain "[\"sand\",  \"lava\"],"
    }
}
