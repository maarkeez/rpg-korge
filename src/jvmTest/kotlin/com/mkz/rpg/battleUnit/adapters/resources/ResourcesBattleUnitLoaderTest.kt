package com.mkz.rpg.battleUnit.adapters.resources

import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResourcesBattleUnitLoaderTest {
    @Test
    fun `should load one deployment per toml file when battle unit resources are initialised`() {
        // Given
        val battleUnitRoot =
            MemoryVfsMix(
                "battleUnit/player-1-unit-1.toml" to "unitId = \"knight\"\nplayerId = \"player-one\"\nrow = 6\ncolumn = 7\n",
            )["battleUnit"]
        val loader = ResourcesBattleUnitLoader(battleUnitRoot = battleUnitRoot)
        // When
        runBlocking { loader.initResources() }
        // Then
        assertThat(loader.loadDeployments()).containsExactly(
            Deployment(battleUnitId = "player-1-unit-1", unitId = "knight", playerId = "player-one", row = 6, column = 7),
        )
    }

    @Test
    fun `should fail with the file when a field is missing`() {
        // Given
        val battleUnitRoot = MemoryVfsMix("battleUnit/player-1-unit-1.toml" to "unitId = \"knight\"\n")["battleUnit"]
        val loader = ResourcesBattleUnitLoader(battleUnitRoot = battleUnitRoot)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("battleUnit/player-1-unit-1.toml")
    }
}
