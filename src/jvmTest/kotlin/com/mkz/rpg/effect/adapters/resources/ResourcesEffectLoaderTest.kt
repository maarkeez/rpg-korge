package com.mkz.rpg.effect.adapters.resources

import com.mkz.rpg.effect.domain.EffectMother.applyEffectOnNearbyAlliesEffect
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.teleportEffect
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResourcesEffectLoaderTest {
    @Test
    fun `should load one effect per toml file when effect resources are initialised`() {
        // Given
        val effectRoot =
            MemoryVfsMix(
                "effect/venom.toml" to "[outcome]\ntype = \"DECREASE_HEALTH\"\ndamage = 3\n\n[application]\ntype = \"ON_TURN_STARTED\"\nduration = 5\n",
                "effect/heal.toml" to "[outcome]\ntype = \"INCREASE_HEALTH\"\nhealing = 7\n\n[application]\ntype = \"IMMEDIATELY\"\n",
                "effect/teleport.toml" to "[outcome]\ntype = \"TELEPORT\"\n\n[application]\ntype = \"IMMEDIATELY\"\n",
                "effect/curse.toml" to "[outcome]\ntype = \"APPLY_EFFECT_ON_NEARBY_ALLIES\"\neffectId = \"venom\"\n\n[application]\ntype = \"ON_DEFEATED\"\n",
                "effect/summon.toml" to "[outcome]\ntype = \"DEPLOY_BATTLE_UNIT\"\nunitId = \"bee\"\n\n[application]\ntype = \"IMMEDIATELY\"\n",
            )["effect"]
        val loader = ResourcesEffectLoader(effectRoot = effectRoot)
        // When
        runBlocking { loader.initResources() }
        // Then
        assertThat(loader.loadEffects()).containsExactly(
            applyEffectOnNearbyAlliesEffect(id = "curse", effectId = "venom").toDto(),
            increaseHealthEffect(id = "heal", healing = 7).toDto(),
            deployBattleUnitEffect(id = "summon", unitId = "bee").toDto(),
            teleportEffect(id = "teleport").toDto(),
            decreaseHealthEffect(id = "venom", damage = 3).toDto(),
        )
    }

    @Test
    fun `should fail with the file when a field required by the outcome type is missing`() {
        // Given
        val effectRoot =
            MemoryVfsMix(
                "effect/venom.toml" to "[outcome]\ntype = \"DECREASE_HEALTH\"\n\n[application]\ntype = \"IMMEDIATELY\"\n",
            )["effect"]
        val loader = ResourcesEffectLoader(effectRoot = effectRoot)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("effect/venom.toml")
            .hasMessageContaining("'outcome.damage' is required")
    }

    @Test
    fun `should fail with the file when the application type is unknown`() {
        // Given
        val effectRoot =
            MemoryVfsMix(
                "effect/venom.toml" to "[outcome]\ntype = \"TELEPORT\"\n\n[application]\ntype = \"SOMETIMES\"\n",
            )["effect"]
        val loader = ResourcesEffectLoader(effectRoot = effectRoot)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("effect/venom.toml")
            .hasMessageContaining("SOMETIMES")
    }
}
