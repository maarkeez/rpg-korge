package com.mkz.rpg.player.adapters.resources

import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesPlayerLoaderTest {
    @Test
    fun `should load one player per toml file when player resources are initialised`() =
        runTest {
            // Given
            val playerRoot =
                MemoryVfsMix(
                    "player/player-one.toml" to "name = \"Human\"\ntype = \"HUMAN\"\n",
                    "player/player-two.toml" to "name = \"CPU\"\ntype = \"CPU\"\n",
                )["player"]
            val loader = ResourcesPlayerLoader(playerRoot = playerRoot)
            // When
            loader.initResources()
            // Then
            loader.loadPlayers().shouldContainExactly(
                player(id = "player-one", name = "Human", type = HUMAN).toDto(),
                player(id = "player-two", name = "CPU", type = CPU).toDto(),
            )
        }

    @Test
    fun `should fail with the file when the player type is unknown`() =
        runTest {
            // Given
            val playerRoot = MemoryVfsMix("player/player-one.toml" to "name = \"Human\"\ntype = \"ROBOT\"\n")["player"]
            val loader = ResourcesPlayerLoader(playerRoot = playerRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("player/player-one.toml").shouldContain("ROBOT")
        }
}
