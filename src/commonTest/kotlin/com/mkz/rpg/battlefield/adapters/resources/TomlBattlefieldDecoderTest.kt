package com.mkz.rpg.battlefield.adapters.resources

import com.mkz.rpg.battlefield.domain.BattlefieldError.InvalidBattlefieldFormat
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TomlBattlefieldDecoderTest {
    private val decoder = TomlBattlefieldDecoder()

    @Test
    fun `should read the tiles as rows when an encoded battlefield is decoded`() {
        // Given
        val tiles = listOf(listOf("sand", "void", "grass"), listOf("water", "sand", "lava"))
        // When
        val decodedTiles = decoder.decode(TomlBattlefieldEncoder().encode(tiles))
        // Then
        decodedTiles shouldBe tiles
    }

    @Test
    fun `should read the game battlefield as the loader does when its resource is decoded`() =
        runTest {
            // Given
            val resource = resourcesVfs["battlefield/battlefield.toml"]
            val loader = ResourcesBattlefieldLoader(battlefieldFile = resource).also { loader -> loader.initResources() }
            // When
            val decodedTiles = decoder.decode(resource.readString())
            // Then
            decodedTiles shouldBe loader.loadTiles()
        }

    @Test
    fun `should fail when the text is not a battlefield`() {
        // When
        val decode = { decoder.decode("Hello, map builder!") }
        // Then
        shouldThrow<InvalidBattlefieldFormat> { decode() }
    }

    @Test
    fun `should fail when the rows have different number of columns`() {
        // When
        val decode = { decoder.decode("tiles = [[\"sand\", \"void\"], [\"grass\"]]\n") }
        // Then
        shouldThrow<InvalidBattlefieldFormat> { decode() }
    }
}
