package com.mkz.rpg.battle.adapters.resources

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesBattleLoaderTest {
    @Test
    fun `should load the player order when the battle resource is initialised`() =
        runTest {
            // Given
            val battleFile = MemoryVfsMix("battle/battle.toml" to "playerOrder = [\"player-one\", \"player-two\"]\n")["battle/battle.toml"]
            val loader = ResourcesBattleLoader(battleFile = battleFile)
            // When
            loader.initResources()
            // Then
            loader.loadPlayerOrder().shouldContainExactly("player-one", "player-two")
        }

    @Test
    fun `should fail with the file when there are no players`() =
        runTest {
            // Given
            val battleFile = MemoryVfsMix("battle/battle.toml" to "playerOrder = []\n")["battle/battle.toml"]
            val loader = ResourcesBattleLoader(battleFile = battleFile)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("battle/battle.toml")
        }
}
