package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AbilityInfoResourcesTest {
    @Test
    fun `AbilityInfoMapper should map every ability when it reads the game resources`() =
        runTest {
            // Given
            val abilities = ResourcesAbilityLoader().also { loader -> loader.initResources() }.loadAbilities()
            val effectsById = ResourcesEffectLoader().also { loader -> loader.initResources() }.loadEffects().associateBy { effect -> effect.id }
            val unitsById = ResourcesUnitLoader().also { loader -> loader.initResources() }.loadUnits().associateBy { unit -> unit.id }
            // When
            val failures =
                abilities.mapNotNull { ability ->
                    runCatching { AbilityInfoMapper.abilityInfo(ability, effectsById::get, unitsById::get) }.exceptionOrNull()?.message
                }
            // Then
            abilities.shouldNotBeEmpty()
            withClue("Abilities the ability info can not tell: $failures") { failures.shouldBeEmpty() }
        }
}
