package com.mkz.rpg.battlefield.adapters.resources

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesBattlefieldLoaderTest {
    @Test
    fun `should load the tiles as rows when the battlefield resource is initialised`() =
        runTest {
            // Given
            val battlefieldFile =
                MemoryVfsMix(
                    "battlefield/battlefield.toml" to "tiles = [\n    [\"sand\", \"void\"],\n    [\"grass\", \"sand\"],\n]\n",
                )["battlefield/battlefield.toml"]
            val loader = ResourcesBattlefieldLoader(battlefieldFile = battlefieldFile)
            // When
            loader.initResources()
            // Then
            loader.loadTiles() shouldBe listOf(listOf("sand", "void"), listOf("grass", "sand"))
        }

    @Test
    fun `should fail with the file when the rows have different number of columns`() =
        runTest {
            // Given
            val battlefieldFile =
                MemoryVfsMix("battlefield/battlefield.toml" to "tiles = [[\"sand\", \"void\"], [\"grass\"]]\n")["battlefield/battlefield.toml"]
            val loader = ResourcesBattlefieldLoader(battlefieldFile = battlefieldFile)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("battlefield/battlefield.toml").shouldContain("same number of columns")
        }

    @Test
    fun `should fail with the file when the battlefield resource does not exist`() =
        runTest {
            // Given
            val battlefieldFile = MemoryVfsMix()["battlefield/battlefield.toml"]
            val loader = ResourcesBattlefieldLoader(battlefieldFile = battlefieldFile)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("battlefield/battlefield.toml")
        }
}
