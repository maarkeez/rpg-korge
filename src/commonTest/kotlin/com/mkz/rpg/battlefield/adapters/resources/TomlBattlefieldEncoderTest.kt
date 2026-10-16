package com.mkz.rpg.battlefield.adapters.resources

import io.kotest.matchers.shouldBe
import korlibs.io.file.std.MemoryVfsMix
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TomlBattlefieldEncoderTest {
    private val encoder = TomlBattlefieldEncoder()

    private suspend fun load(toml: String): List<List<String>> {
        val loader = ResourcesBattlefieldLoader(battlefieldFile = MemoryVfsMix("battlefield/battlefield.toml" to toml)["battlefield/battlefield.toml"])
        loader.initResources()
        return loader.loadTiles()
    }

    @Test
    fun `should write one row per line with the terrain ids aligned when the tiles are encoded`() {
        // Given
        val tiles = listOf(listOf("sand", "water"), listOf("lava", "sand"))
        // When
        val toml = encoder.encode(tiles)
        // Then
        toml shouldBe
            """
            |# Terrain id of each tile. Each line is a row of the battlefield, from top to bottom.
            |tiles = [
            |    ["sand",   "water"],
            |    ["lava",   "sand"],
            |]
            |
            """.trimMargin()
    }

    @Test
    fun `should be loaded as the same tiles when the encoded tiles are loaded as the battlefield`() =
        runTest {
            // Given
            val tiles = listOf(listOf("sand", "void", "grass"), listOf("water", "sand", "lava"))
            // When
            val loadedTiles = load(encoder.encode(tiles))
            // Then
            loadedTiles shouldBe tiles
        }

    @Test
    fun `should write the game battlefield exactly as its resource when its tiles are encoded`() =
        runTest {
            // Given
            val resource = resourcesVfs["battlefield/battlefield.toml"].readString()
            // When
            val toml = encoder.encode(load(resource))
            // Then
            toml shouldBe resource
        }
}
