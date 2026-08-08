package unit.domain

import unit.domain.UnitEvent.UnitCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Unit private constructor(
    private val id: Id,
    private val name: Name,
    private val healthPoints: HealthPoints,
    private val manaPoints: ManaPoints,
    private val abilities: Abilities,
    private val movementRange: MovementRange,
    private val events: Set<UnitEvent>,
) {

    companion object {
        fun create(unitDto: Dto): Unit {
            return Unit(
                id = Id(unitDto.id),
                name = Name(unitDto.name),
                healthPoints = HealthPoints(unitDto.healthPoints),
                manaPoints = ManaPoints(unitDto.manaPoints),
                abilities = Abilities(unitDto.abilities),
                movementRange = MovementRange(unitDto.movementRange),
                events = setOf(UnitCreated(unitDto.id))
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())
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
