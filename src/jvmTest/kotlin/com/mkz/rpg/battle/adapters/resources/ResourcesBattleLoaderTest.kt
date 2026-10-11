package com.mkz.rpg.battle.adapters.resources

import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResourcesBattleLoaderTest {
    @Test
    fun `should load the player order when the battle resource is initialised`() {
        // Given
        val battleFile = MemoryVfsMix("battle/battle.toml" to "playerOrder = [\"player-one\", \"player-two\"]\n")["battle/battle.toml"]
        val loader = ResourcesBattleLoader(battleFile = battleFile)
        // When
        runBlocking { loader.initResources() }
        // Then
        assertThat(loader.loadPlayerOrder()).containsExactly("player-one", "player-two")
    }

    @Test
    fun `should fail with the file when there are no players`() {
        // Given
        val battleFile = MemoryVfsMix("battle/battle.toml" to "playerOrder = []\n")["battle/battle.toml"]
        val loader = ResourcesBattleLoader(battleFile = battleFile)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("battle/battle.toml")
    }
}
