package com.mkz.rpg.unit.domain

import com.mkz.rpg.ability.domain.AbilityMother.ability
import korlibs.io.util.UUID
import kotlin.random.Random

object UnitMother {
    fun unit(
        id: String = id(),
        baseMaximumHealth: Int = baseMaximumHealth(),
        baseMaximumMana: Int = baseMaximumMana(),
        startingAbilities: List<String> = startingAbilities(),
        maximumSteps: Int = maximumSteps(),
        startingGold: Int = 0,
    ) = Unit
        .create(
            unitDto =
                Unit.Dto(
                    id = id,
                    baseMaximumHealth = baseMaximumHealth,
                    baseMaximumMana = baseMaximumMana,
                    startingAbilities = startingAbilities,
                    maximumSteps = maximumSteps,
                    startingGold = startingGold,
                ),
        ).pullEvents()
        .second

    fun id() = "unit-${UUID.randomUUID()}"

    fun baseMaximumHealth() = Random.nextInt(1, 99)

    fun baseMaximumMana() = Random.nextInt(0, 99)

    fun startingAbilities() = List(Random.nextInt(1, 4)) { ability().toDto().id }

    fun maximumSteps() = Random.nextInt(1, 5)
}
