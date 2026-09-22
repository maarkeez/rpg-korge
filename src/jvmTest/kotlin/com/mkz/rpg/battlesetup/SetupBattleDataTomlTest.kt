package com.mkz.rpg.battlesetup

import com.mkz.rpg.battlesetup.usecases.AbilitiesFile
import com.mkz.rpg.battlesetup.usecases.BattlefieldFile
import com.mkz.rpg.battlesetup.usecases.DeploymentsFile
import com.mkz.rpg.battlesetup.usecases.EffectsFile
import com.mkz.rpg.battlesetup.usecases.PlayersFile
import com.mkz.rpg.battlesetup.usecases.TerrainsFile
import com.mkz.rpg.battlesetup.usecases.UnitsFile
import com.mkz.rpg.shared.adapters.toml.TomlService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SetupBattleDataTomlTest {
    private val tomlService = TomlService()

    private fun readResource(path: String): String {
        val clazz = SetupBattleDataTomlTest::class.java
        val url =
            clazz.getResource("/$path")
                ?: throw IllegalStateException("Resource not found: $path")
        return url.readText()
    }

    @Test
    fun `should deserialize players toml`() {
        val file = tomlService.deserialize<PlayersFile>(readResource("setupBattle/players.toml"))
        assertThat(file.player).hasSize(2)
        assertThat(file.player[0].id).isEqualTo("player-one")
    }

    @Test
    fun `should deserialize terrains toml`() {
        val file = tomlService.deserialize<TerrainsFile>(readResource("setupBattle/terrains.toml"))
        assertThat(file.terrain.map { it.id }).containsExactly("sand", "void")
    }

    @Test
    fun `should deserialize battlefield toml`() {
        val file = tomlService.deserialize<BattlefieldFile>(readResource("setupBattle/battlefield.toml"))
        assertThat(file.rows).isEqualTo(8)
        assertThat(file.columns).isEqualTo(8)
        assertThat(file.tiles).hasSize(8)
        assertThat(file.tiles[3]).containsExactly("sand", "sand", "void", "void", "void", "void", "sand", "sand")
    }

    @Test
    fun `should deserialize effects toml`() {
        val file = tomlService.deserialize<EffectsFile>(readResource("setupBattle/effects.toml"))
        assertThat(file.effects).hasSize(6)
        val venom = file.effects.first { it.id == "venom-damage" }
        assertThat(venom.outcome.type.name).isEqualTo("DECREASE_HEALTH")
        assertThat(venom.outcome.decreaseHealth?.damage).isEqualTo(3)
        assertThat(venom.application.type.name).isEqualTo("ON_TURN_STARTED")
        assertThat(venom.application.onTurnStarted?.duration).isEqualTo(5)
    }

    @Test
    fun `should deserialize abilities toml`() {
        val file = tomlService.deserialize<AbilitiesFile>(readResource("setupBattle/abilities.toml"))
        assertThat(file.abilities).hasSize(7)
        val skull = file.abilities.first { it.id == "skull" }
        assertThat(skull.effectSpecs).hasSize(2)
        assertThat(skull.targeting.name).isEqualTo("ADJACENT_ENEMY")
    }

    @Test
    fun `should deserialize units toml`() {
        val file = tomlService.deserialize<UnitsFile>(readResource("setupBattle/units.toml"))
        assertThat(file.unit).hasSize(3)
        val knight = file.unit.first { it.id == "knight" }
        assertThat(knight.abilities).containsExactly("poisoned-sword", "mushroom", "skull", "teleport", "bee", "heal")
    }

    @Test
    fun `should deserialize deployments toml`() {
        val file = tomlService.deserialize<DeploymentsFile>(readResource("setupBattle/deployments.toml"))
        assertThat(file.firstRoundPlayers).containsExactly("player-one", "player-two")
        assertThat(file.deployment).hasSize(4)
        assertThat(file.deployment[0].battleUnitId).isEqualTo("player-2-unit-1")
    }
}
