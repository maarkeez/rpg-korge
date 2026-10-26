package com.mkz.rpg.unit.domain

import com.mkz.rpg.ability.domain.AbilityMother.ability
import korlibs.io.util.UUID
import kotlin.random.Random

object UnitMother {
    fun unit(
        id: String = id(),
        healthPoints: Int = healthPoints(),
        manaPoints: Int = manaPoints(),
        abilities: List<String> = abilities(),
        movementRange: Int = movementRange(),
        startingGold: Int = 0,
    ) = Unit
        .create(
            unitDto =
                Unit.Dto(
                    id = id,
                    healthPoints = healthPoints,
                    manaPoints = manaPoints,
                    abilities = abilities,
                    movementRange = movementRange,
                    startingGold = startingGold,
                ),
        ).pullEvents()
        .second

    fun id() = "unit-${UUID.randomUUID()}"

    fun healthPoints() = Random.nextInt(1, 99)

    fun manaPoints() = Random.nextInt(0, 99)

    fun abilities() = List(Random.nextInt(1, 4)) { ability().toDto().id }

    fun movementRange() = Random.nextInt(1, 5)
}
