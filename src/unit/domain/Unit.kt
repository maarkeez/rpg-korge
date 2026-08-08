package unit.domain

import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Unit private constructor(
    private val id: Id,
    private val name: Name,
    private val healthPoints: HealthPoints,
    private val manaPoints: ManaPoints,
    private val abilities: Abilities,
    private val movementRange: MovementRange,
) {

    fun toDto() = Dto(
        id = id.value,
        name = name.value,
        healthPoints = healthPoints.value,
        manaPoints = manaPoints.value,
        abilities = abilities.value,
        movementRange = movementRange.value,
    )

    @JvmInline value class Id(val value: String)
    @JvmInline value class Name(val value: String)
    @JvmInline value class HealthPoints(val value: Int)
    @JvmInline value class ManaPoints(val value: Int)
    @JvmInline value class Abilities(val value: List<String>)
    @JvmInline value class MovementRange(val value: Int)

    data class Dto(
        val id: String,
        val name: String,
        val healthPoints: Int,
        val manaPoints: Int,
        val abilities: List<String>,
        val movementRange: Int,
    )
}
