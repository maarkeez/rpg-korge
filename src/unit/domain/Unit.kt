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
    
    @JvmInline value class Id(val value: String)
    @JvmInline value class Name(val value: String)
    @JvmInline value class HealthPoints(val value: Int)
    @JvmInline value class ManaPoints(val value: Int)
    @JvmInline value class Abilities(val value: List<String>)
    @JvmInline value class MovementRange(val value: Int)
}
