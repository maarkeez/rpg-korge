package com.mkz.rpg.unit.domain

import com.mkz.rpg.unit.domain.UnitError.NegativeStartingGold
import com.mkz.rpg.unit.domain.UnitError.StartingAbilitiesAboveMaximum
import com.mkz.rpg.unit.domain.UnitEvent.UnitCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Unit private constructor(
    private val id: Id,
    private val baseMaximumHealth: BaseMaximumHealth,
    private val baseMaximumMana: BaseMaximumMana,
    private val startingAbilities: StartingAbilities,
    private val maximumSteps: MaximumSteps,
    private val startingGold: StartingGold,
    private val events: Set<UnitEvent>,
) {
    companion object {
        private const val MAXIMUM_ABILITIES = 6

        fun maximumAbilities(): Int = MAXIMUM_ABILITIES

        fun create(unitDto: Dto): Unit =
            Unit(
                id = Id(unitDto.id),
                baseMaximumHealth = BaseMaximumHealth(unitDto.baseMaximumHealth),
                baseMaximumMana = BaseMaximumMana(unitDto.baseMaximumMana),
                startingAbilities = StartingAbilities(unitDto.startingAbilities),
                maximumSteps = MaximumSteps(unitDto.maximumSteps),
                startingGold = StartingGold(unitDto.startingGold),
                events = setOf(UnitCreated(unitDto.id)),
            )
    }

    fun pullEvents(): Pair<Set<UnitEvent>, Unit> = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            baseMaximumHealth = baseMaximumHealth.value,
            baseMaximumMana = baseMaximumMana.value,
            startingAbilities = startingAbilities.value,
            maximumSteps = maximumSteps.value,
            startingGold = startingGold.value,
        )

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class BaseMaximumHealth(
        val value: Int,
    )

    @JvmInline private value class BaseMaximumMana(
        val value: Int,
    )

    @JvmInline private value class StartingAbilities(
        val value: List<String>,
    ) {
        init {
            if (value.size > MAXIMUM_ABILITIES) throw StartingAbilitiesAboveMaximum()
        }
    }

    @JvmInline private value class MaximumSteps(
        val value: Int,
    )

    @JvmInline private value class StartingGold(
        val value: Int,
    ) {
        init {
            if (value < 0) throw NegativeStartingGold()
        }
    }

    data class Dto(
        val id: String,
        val baseMaximumHealth: Int,
        val baseMaximumMana: Int,
        val startingAbilities: List<String>,
        val maximumSteps: Int,
        val startingGold: Int,
    )
}
