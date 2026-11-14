package com.mkz.tessavar.ability.adapters.resources

import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto.Type.CASTER
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_TILE
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesAbilityLoaderTest {
    @Test
    fun `should load one ability per toml file when ability resources are initialised`() =
        runTest {
            // Given
            val abilityRoot =
                MemoryVfsMix(
                    "ability/skull.toml" to
                        """
                        manaCost = 10
                        cooldown = 1
                        targeting = "ADJACENT_ENEMY"

                        [[effects]]
                        effectId = "damage"
                        target = "TARGETED_BATTLE_UNIT"

                        [[effects]]
                        effectId = "heal"
                        target = "CASTER"
                        """.trimIndent(),
                )["ability"]
            val loader = ResourcesAbilityLoader(abilityRoot = abilityRoot)
            // When
            loader.initResources()
            // Then
            loader.loadAbilities().shouldContainExactly(
                ability(
                    id = "skull",
                    manaCost = 10,
                    cooldown = 1,
                    targeting = ADJACENT_ENEMY,
                    abilityEffects = listOf(abilityEffect(effectId = "damage", target = TARGETED_BATTLE_UNIT), abilityEffect(effectId = "heal", target = CASTER)),
                ).toDto(),
            )
        }

    @Test
    fun `should load the summon duration of an ability deploying a battle unit`() =
        runTest {
            // Given
            val abilityRoot =
                MemoryVfsMix(
                    "ability/bat.toml" to
                        """
                        manaCost = 10
                        cooldown = 0
                        targeting = "VACANT_TILE_ADJACENT_TO_CASTER"
                        summonDuration = 5

                        [[effects]]
                        effectId = "deploy-bat"
                        target = "TARGETED_TILE"
                        """.trimIndent(),
                )["ability"]
            val loader = ResourcesAbilityLoader(abilityRoot = abilityRoot)
            // When
            loader.initResources()
            // Then
            loader.loadAbilities().shouldContainExactly(
                ability(
                    id = "bat",
                    manaCost = 10,
                    cooldown = 0,
                    targeting = VACANT_TILE_ADJACENT_TO_CASTER,
                    abilityEffects = listOf(abilityEffect(effectId = "deploy-bat", target = TARGETED_TILE)),
                    summonDuration = 5,
                ).toDto(),
            )
        }

    @Test
    fun `should fail with the file when the targeting is unknown`() =
        runTest {
            // Given
            val abilityRoot =
                MemoryVfsMix(
                    "ability/skull.toml" to "manaCost = 10\ncooldown = 1\ntargeting = \"ANYWHERE\"\n\n[[effects]]\neffectId = \"damage\"\ntarget = \"TARGETED_BATTLE_UNIT\"\n",
                )["ability"]
            val loader = ResourcesAbilityLoader(abilityRoot = abilityRoot)
            // When
            val initialisation = suspend { loader.initResources() }
            // Then
            shouldThrow<IllegalStateException> { initialisation() }.message.shouldContain("ability/skull.toml").shouldContain("ANYWHERE")
        }
}
