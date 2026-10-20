package com.mkz.rpg.unit.domain

import com.mkz.rpg.shared.domain.Gold
import com.mkz.rpg.unit.domain.UnitError.NegativeStartingGold
import com.mkz.rpg.unit.domain.UnitError.StartingGoldAboveLimit
import com.mkz.rpg.unit.domain.UnitEvent.UnitCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Unit private constructor(
    private val id: Id,
    private val name: Name,
    private val healthPoints: HealthPoints,
    private val manaPoints: ManaPoints,
    private val abilities: Abilities,
    private val movementRange: MovementRange,
    private val startingGold: StartingGold,
    private val events: Set<UnitEvent>,
) {
    companion object {
        fun create(unitDto: Dto): Unit =
            Unit(
                id = Id(unitDto.id),
                name = Name(unitDto.name),
                healthPoints = HealthPoints(unitDto.healthPoints),
                manaPoints = ManaPoints(unitDto.manaPoints),
                abilities = Abilities(unitDto.abilities),
                movementRange = MovementRange(unitDto.movementRange),
                startingGold = StartingGold(unitDto.startingGold),
                events = setOf(UnitCreated(unitDto.id)),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            name = name.value,
            healthPoints = healthPoints.value,
            manaPoints = manaPoints.value,
            abilities = abilities.value,
            movementRange = movementRange.value,
            startingGold = startingGold.value,
        )

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class Name(
        val value: String,
    )

    @JvmInline private value class HealthPoints(
        val value: Int,
    )

    @JvmInline private value class ManaPoints(
        val value: Int,
    )

    @JvmInline private value class Abilities(
        val value: List<String>,
    )

    @JvmInline private value class MovementRange(
        val value: Int,
    )

    // The gold each battle unit of the unit carries once deployed.
    @JvmInline private value class StartingGold(
        val value: Int,
    ) {
        init {
            if (value < 0) throw NegativeStartingGold()
            if (value > Gold.MAXIMUM) throw StartingGoldAboveLimit()
        }
    }

    data class Dto(
        val id: String,
        val name: String,
        val healthPoints: Int,
        val manaPoints: Int,
        val abilities: List<String>,
        val movementRange: Int,
        val startingGold: Int = 0,
    )
}
