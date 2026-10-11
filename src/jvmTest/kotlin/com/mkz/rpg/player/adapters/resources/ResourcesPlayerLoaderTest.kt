package com.mkz.rpg.player.adapters.resources

import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResourcesPlayerLoaderTest {
    @Test
    fun `should load one player per toml file when player resources are initialised`() {
        // Given
        val playerRoot =
            MemoryVfsMix(
                "player/player-one.toml" to "name = \"Human\"\ntype = \"HUMAN\"\n",
                "player/player-two.toml" to "name = \"CPU\"\ntype = \"CPU\"\n",
            )["player"]
        val loader = ResourcesPlayerLoader(playerRoot = playerRoot)
        // When
        runBlocking { loader.initResources() }
        // Then
        assertThat(loader.loadPlayers()).containsExactly(
            player(id = "player-one", name = "Human", type = HUMAN).toDto(),
            player(id = "player-two", name = "CPU", type = CPU).toDto(),
        )
    }

    @Test
    fun `should fail with the file when the player type is unknown`() {
        // Given
        val playerRoot = MemoryVfsMix("player/player-one.toml" to "name = \"Human\"\ntype = \"ROBOT\"\n")["player"]
        val loader = ResourcesPlayerLoader(playerRoot = playerRoot)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("player/player-one.toml")
            .hasMessageContaining("ROBOT")
    }
}
