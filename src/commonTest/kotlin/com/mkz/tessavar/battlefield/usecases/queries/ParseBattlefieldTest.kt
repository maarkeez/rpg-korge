package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.domain.BattlefieldError.InvalidBattlefieldFormat
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ParseBattlefieldTest {
    private val parseBattlefield = ParseBattlefield(TomlBattlefieldDecoder())

    @Test
    fun `should read the terrain id of each tile when the text is a battlefield`() {
        // When
        val tiles = parseBattlefield("tiles = [\n    [\"sand\", \"lava\"],\n]\n")
        // Then
        tiles shouldBe listOf(listOf("sand", "lava"))
    }

    @Test
    fun `should fail when the text is not a battlefield`() {
        // When
        val parse = { parseBattlefield("tiles = 3") }
        // Then
        shouldThrow<InvalidBattlefieldFormat> { parse() }
    }
}
