package com.mkz.rpg.battlefield.adapters.resources

import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResourcesBattlefieldLoaderTest {
    @Test
    fun `should load the tiles as rows when the battlefield resource is initialised`() {
        // Given
        val battlefieldFile =
            MemoryVfsMix(
                "battlefield/battlefield.toml" to "tiles = [\n    [\"sand\", \"void\"],\n    [\"grass\", \"sand\"],\n]\n",
            )["battlefield/battlefield.toml"]
        val loader = ResourcesBattlefieldLoader(battlefieldFile = battlefieldFile)
        // When
        runBlocking { loader.initResources() }
        // Then
        assertThat(loader.loadTiles()).isEqualTo(listOf(listOf("sand", "void"), listOf("grass", "sand")))
    }

    @Test
    fun `should fail with the file when the rows have different number of columns`() {
        // Given
        val battlefieldFile =
            MemoryVfsMix("battlefield/battlefield.toml" to "tiles = [[\"sand\", \"void\"], [\"grass\"]]\n")["battlefield/battlefield.toml"]
        val loader = ResourcesBattlefieldLoader(battlefieldFile = battlefieldFile)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("battlefield/battlefield.toml")
            .hasMessageContaining("same number of columns")
    }

    @Test
    fun `should fail with the file when the battlefield resource does not exist`() {
        // Given
        val battlefieldFile = MemoryVfsMix()["battlefield/battlefield.toml"]
        val loader = ResourcesBattlefieldLoader(battlefieldFile = battlefieldFile)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("battlefield/battlefield.toml")
    }
}
