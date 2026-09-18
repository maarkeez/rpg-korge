package com.mkz.rpg.ability.domain

import com.mkz.rpg.ability.domain.AbilityError.AbilityCooldownAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityCostAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectsAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEmptyEffects
import com.mkz.rpg.ability.domain.AbilityError.AbilityNameTooLong
import com.mkz.rpg.ability.domain.AbilityError.EmptyAbilityId
import com.mkz.rpg.ability.domain.AbilityError.EmptyAbilityName
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityCooldown
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityCost
import com.mkz.rpg.ability.domain.AbilityEvent.AbilityCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Ability private constructor(
    private val id: Id,
    private val name: Name,
    private val cost: Cost,
    private val cooldown: Cooldown,
    private val targeting: Targeting,
    private val effectSpecs: EffectSpecs,
    private val events: Set<AbilityEvent>,
) {
    companion object {
        fun create(dto: Dto): Ability =
            Ability(
                id = Id(dto.id),
                name = Name(dto.name),
                cost = Cost(dto.cost),
                cooldown = Cooldown(dto.cooldown),
                targeting = Targeting(dto.targeting),
                effectSpecs = EffectSpecs.fromDtos(dto.effectSpecs),
                events = setOf(AbilityCreated(abilityId = dto.id)),
            )
    }

    fun toDto() =
        Dto(
            id = id.value,
            name = name.value,
            cost = cost.value,
            cooldown = cooldown.value,
            targeting = targeting.toDto(),
            effectSpecs = effectSpecs.toDto(),
        )

    fun pullEvents() = events to copy(events = emptySet())

    @JvmInline private value class Id(
        val value: String,
    ) {
        init {
            if (value.isBlank()) throw EmptyAbilityId()
        }
    }

    @JvmInline private value class Name(
        val value: String,
    ) {
        init {
            if (value.isBlank()) throw EmptyAbilityName()
            if (value.count() > 50) throw AbilityNameTooLong()
        }
    }

    @JvmInline private value class Cost(
        val value: Int,
    ) {
        init {
            if (value < 0) throw NegativeAbilityCost()
            if (value > 999) throw AbilityCostAboveLimit()
        }
    }

    @JvmInline private value class Cooldown(
        val value: Int,
    ) {
        init {
            if (value < 0) throw NegativeAbilityCooldown()
            if (value > 99) throw AbilityCooldownAboveLimit()
        }
    }

    private data class EffectSpecs(
        val value: List<EffectSpec>,
    ) {
        init {
            if (value.isEmpty()) throw AbilityEmptyEffects()
            if (value.size > 3) throw AbilityEffectsAboveLimit()
        }

        companion object {
            fun fromDtos(dtos: List<Dto.EffectSpecDto>): EffectSpecs = EffectSpecs(dtos.map { effectSpec -> EffectSpec(effectSpec) })
        }

        fun toDto(): List<Dto.EffectSpecDto> = value.map { effectSpec -> effectSpec.toDto() }

        private data class EffectSpec(
            val effectId: EffectId,
            val target: TargetExpression,
        ) {
            constructor(dto: Dto.EffectSpecDto) : this(EffectId(dto.effectId), TargetExpression(dto.target))

            fun toDto() = Dto.EffectSpecDto(effectId = effectId.value, target = TargetExpression.of(target))

            @JvmInline private value class EffectId(
                val value: String,
            )
        }
    }

    sealed interface TargetExpression {
        data object Caster : TargetExpression

        data object SelectedTarget : TargetExpression

        data object SelectedTile : TargetExpression

        data object CasterTile : TargetExpression

        data object NearbyAllies : TargetExpression

        companion object {
            operator fun invoke(dto: Dto.TargetExpressionDto): TargetExpression =
                when (dto.type) {
                    Dto.TargetExpressionDto.Type.CASTER -> Caster
                    Dto.TargetExpressionDto.Type.SELECTED_TARGET -> SelectedTarget
                    Dto.TargetExpressionDto.Type.SELECTED_TILE -> SelectedTile
                    Dto.TargetExpressionDto.Type.CASTER_TILE -> CasterTile
                    Dto.TargetExpressionDto.Type.NEARBY_ALLIES -> NearbyAllies
                }

            fun of(expression: TargetExpression): Dto.TargetExpressionDto =
                Dto.TargetExpressionDto(
                    type =
                        when (expression) {
                            is Caster -> Dto.TargetExpressionDto.Type.CASTER
                            is SelectedTarget -> Dto.TargetExpressionDto.Type.SELECTED_TARGET
                            is SelectedTile -> Dto.TargetExpressionDto.Type.SELECTED_TILE
                            is CasterTile -> Dto.TargetExpressionDto.Type.CASTER_TILE
                            is NearbyAllies -> Dto.TargetExpressionDto.Type.NEARBY_ALLIES
                        },
                )
        }
    }

    private enum class Targeting {
        SELF,
        ADJACENT_ENEMY,
        ALL_ADJACENT_ENEMIES,
        VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
        VACANT_TILE_ADJACENT_TO_SELF,
        ;

        companion object {
            operator fun invoke(targeting: Dto.TargetingDto): Targeting =
                when (targeting) {
                    Dto.TargetingDto.SELF -> SELF
                    Dto.TargetingDto.ADJACENT_ENEMY -> ADJACENT_ENEMY
                    Dto.TargetingDto.ALL_ADJACENT_ENEMIES -> ALL_ADJACENT_ENEMIES
                    Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT -> VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
                    Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_SELF -> VACANT_TILE_ADJACENT_TO_SELF
                }
        }

        fun toDto() =
            when (this) {
                SELF -> Dto.TargetingDto.SELF
                ADJACENT_ENEMY -> Dto.TargetingDto.ADJACENT_ENEMY
                ALL_ADJACENT_ENEMIES -> Dto.TargetingDto.ALL_ADJACENT_ENEMIES
                VACANT_TILE_ADJACENT_TO_BATTLE_UNIT -> Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
                VACANT_TILE_ADJACENT_TO_SELF -> Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_SELF
            }
    }

    data class Dto(
        val id: String,
        val name: String,
        val cost: Int,
        val cooldown: Int,
        val targeting: TargetingDto,
        val effectSpecs: List<EffectSpecDto>,
    ) {
        enum class TargetingDto {
            SELF,
            ADJACENT_ENEMY,
            ALL_ADJACENT_ENEMIES,
            VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
            VACANT_TILE_ADJACENT_TO_SELF,
        }

        data class EffectSpecDto(
            val effectId: String,
            val target: TargetExpressionDto,
        )

        data class TargetExpressionDto(
            val type: Type,
        ) {
            enum class Type {
                CASTER,
                SELECTED_TARGET,
                SELECTED_TILE,
                CASTER_TILE,
                NEARBY_ALLIES,
            }
        }
    }
}
