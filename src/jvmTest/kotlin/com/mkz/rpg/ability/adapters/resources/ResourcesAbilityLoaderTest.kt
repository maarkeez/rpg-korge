package com.mkz.rpg.ability.adapters.resources

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResourcesAbilityLoaderTest {
    @Test
    fun `should load one ability per toml file when ability resources are initialised`() {
        // Given
        val abilityRoot =
            MemoryVfsMix(
                "ability/skull.toml" to
                    """
                    name = "Skull"
                    cost = 10
                    cooldown = 1
                    targeting = "ADJACENT_ENEMY"

                    [[effects]]
                    effectId = "damage"
                    target = "SELECTED_TARGET"

                    [[effects]]
                    effectId = "heal"
                    target = "CASTER"
                    """.trimIndent(),
            )["ability"]
        val loader = ResourcesAbilityLoader(abilityRoot = abilityRoot)
        // When
        runBlocking { loader.initResources() }
        // Then
        assertThat(loader.loadAbilities()).containsExactly(
            ability(
                id = "skull",
                name = "Skull",
                cost = 10,
                cooldown = 1,
                targeting = ADJACENT_ENEMY,
                effectSpecs = listOf(effectSpec(effectId = "damage", target = SELECTED_TARGET), effectSpec(effectId = "heal", target = CASTER)),
            ).toDto(),
        )
    }

    @Test
    fun `should fail with the file when the targeting is unknown`() {
        // Given
        val abilityRoot =
            MemoryVfsMix(
                "ability/skull.toml" to "name = \"Skull\"\ncost = 10\ncooldown = 1\ntargeting = \"ANYWHERE\"\n\n[[effects]]\neffectId = \"damage\"\ntarget = \"SELECTED_TARGET\"\n",
            )["ability"]
        val loader = ResourcesAbilityLoader(abilityRoot = abilityRoot)
        // When
        val initialisation = { runBlocking { loader.initResources() } }
        // Then
        assertThatThrownBy { initialisation() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("ability/skull.toml")
            .hasMessageContaining("ANYWHERE")
    }
}
