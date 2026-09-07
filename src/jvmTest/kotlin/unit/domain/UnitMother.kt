package unit.domain

import ability.domain.*
import korlibs.io.util.*
import kotlin.random.*

object UnitMother {

    fun unit(
        id: String = id(),
        name: String = name(),
        healthPoints: Int = healthPoints(),
        manaPoints: Int = manaPoints(),
        abilities: List<String> = abilities(),
        movementRange: Int = movementRange(),
    ) = Unit.create(unitDto=Unit.Dto(
        id = id,
        name = name,
        healthPoints = healthPoints,
        manaPoints = manaPoints,
        abilities = abilities,
        movementRange = movementRange,
    ))

    fun id() = "unit-${Random.nextInt(1, 100)}"
    fun name() = "Unit ${UUID.randomUUID().toString().takeLast(5)}"
    fun healthPoints() = Random.nextInt(1, 99)
    fun manaPoints() = Random.nextInt(0, 99)
    fun abilities() = List(Random.nextInt(1, 4)) { AbilityMother.ability().toDto().id }
    fun movementRange() = Random.nextInt(1, 5)
}
