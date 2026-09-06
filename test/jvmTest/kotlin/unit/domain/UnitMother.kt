package unit.domain

import ability.domain.AbilityMother
import korlibs.crypto.SecureRandom.nextInt
import korlibs.io.util.UUID

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

    fun id() = "unit-${nextInt(from = 1, until = 100)}"
    fun name() = "Unit ${UUID.randomUUID().toString().takeLast(5)}"
    fun healthPoints() = nextInt(from = 1, until = 99)
    fun manaPoints() = nextInt(from = 0, until = 99)
    fun abilities() = List(nextInt(from = 1, until = 4)) { AbilityMother.ability().toDto().id }
    fun movementRange() = nextInt(from = 1, until = 5)
}
