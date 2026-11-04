package com.mkz.rpg.ability.domain

import com.mkz.rpg.ability.domain.AbilityError.AbilityCooldownAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEffectsAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilityEmptyEffects
import com.mkz.rpg.ability.domain.AbilityError.AbilityManaCostAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilitySummonDurationAboveLimit
import com.mkz.rpg.ability.domain.AbilityError.AbilitySummonDurationBelowMinimum
import com.mkz.rpg.ability.domain.AbilityError.EmptyAbilityId
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityCooldown
import com.mkz.rpg.ability.domain.AbilityError.NegativeAbilityManaCost
import com.mkz.rpg.ability.domain.AbilityEvent.AbilityCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Ability private constructor(
    private val id: Id,
    private val manaCost: ManaCost,
    private val cooldown: Cooldown,
    private val summonDuration: SummonDuration?,
    private val targeting: Targeting,
    private val abilityEffects: AbilityEffects,
    private val events: Set<AbilityEvent>,
) {
    companion object {
        fun create(dto: Dto): Ability =
            Ability(
                id = Id(dto.id),
                manaCost = ManaCost(dto.manaCost),
                cooldown = Cooldown(dto.cooldown),
                summonDuration = dto.summonDuration?.let(::SummonDuration),
                targeting = Targeting(dto.targeting),
                abilityEffects = AbilityEffects.fromDtos(dto.abilityEffects),
                events = setOf(AbilityCreated(abilityId = dto.id)),
            )
    }

    fun toDto() =
        Dto(
            id = id.value,
            manaCost = manaCost.value,
            cooldown = cooldown.value,
            targeting = targeting.toDto(),
            abilityEffects = abilityEffects.toDto(),
            summonDuration = summonDuration?.value,
        )

    fun pullEvents() = events to copy(events = emptySet())

    @JvmInline private value class Id(
        val value: String,
    ) {
        init {
            if (value.isBlank()) throw EmptyAbilityId()
        }
    }

    @JvmInline private value class ManaCost(
        val value: Int,
    ) {
        init {
            if (value < 0) throw NegativeAbilityManaCost()
            if (value > 999) throw AbilityManaCostAboveLimit()
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

    // How many turns of its player the battle unit the ability deploys lasts.
    @JvmInline private value class SummonDuration(
        val value: Int,
    ) {
        init {
            if (value < 1) throw AbilitySummonDurationBelowMinimum()
            if (value > 99) throw AbilitySummonDurationAboveLimit()
        }
    }

    private data class AbilityEffects(
        val value: List<AbilityEffect>,
    ) {
        init {
            if (value.isEmpty()) throw AbilityEmptyEffects()
            if (value.size > 3) throw AbilityEffectsAboveLimit()
        }

        companion object {
            fun fromDtos(dtos: List<Dto.AbilityEffectDto>): AbilityEffects = AbilityEffects(dtos.map { abilityEffect -> AbilityEffect(abilityEffect) })
        }

        fun toDto(): List<Dto.AbilityEffectDto> = value.map { abilityEffect -> abilityEffect.toDto() }

        private data class AbilityEffect(
            val effectId: EffectId,
            val target: TargetExpression,
        ) {
            constructor(dto: Dto.AbilityEffectDto) : this(EffectId(dto.effectId), TargetExpression(dto.target))

            fun toDto() = Dto.AbilityEffectDto(effectId = effectId.value, target = TargetExpression.of(target))

            @JvmInline private value class EffectId(
                val value: String,
            )
        }
    }

    sealed interface TargetExpression {
        data object Caster : TargetExpression

        data object TargetedBattleUnit : TargetExpression

        data object TargetedTile : TargetExpression

        data object CasterTile : TargetExpression

        data object AdjacentAllies : TargetExpression

        companion object {
            operator fun invoke(dto: Dto.TargetExpressionDto): TargetExpression =
                when (dto.type) {
                    Dto.TargetExpressionDto.Type.CASTER -> Caster
                    Dto.TargetExpressionDto.Type.TARGETED_BATTLE_UNIT -> TargetedBattleUnit
                    Dto.TargetExpressionDto.Type.TARGETED_TILE -> TargetedTile
                    Dto.TargetExpressionDto.Type.CASTER_TILE -> CasterTile
                    Dto.TargetExpressionDto.Type.ADJACENT_ALLIES -> AdjacentAllies
                }

            fun of(expression: TargetExpression): Dto.TargetExpressionDto =
                Dto.TargetExpressionDto(
                    type =
                        when (expression) {
                            is Caster -> Dto.TargetExpressionDto.Type.CASTER
                            is TargetedBattleUnit -> Dto.TargetExpressionDto.Type.TARGETED_BATTLE_UNIT
                            is TargetedTile -> Dto.TargetExpressionDto.Type.TARGETED_TILE
                            is CasterTile -> Dto.TargetExpressionDto.Type.CASTER_TILE
                            is AdjacentAllies -> Dto.TargetExpressionDto.Type.ADJACENT_ALLIES
                        },
                )
        }
    }

    private enum class Targeting {
        CASTER,
        ADJACENT_ENEMY,
        ALL_ADJACENT_ENEMIES,
        VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT,
        VACANT_TILE_ADJACENT_TO_CASTER,
        ;

        companion object {
            operator fun invoke(targeting: Dto.TargetingDto): Targeting =
                when (targeting) {
                    Dto.TargetingDto.CASTER -> CASTER
                    Dto.TargetingDto.ADJACENT_ENEMY -> ADJACENT_ENEMY
                    Dto.TargetingDto.ALL_ADJACENT_ENEMIES -> ALL_ADJACENT_ENEMIES
                    Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT -> VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT
                    Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER -> VACANT_TILE_ADJACENT_TO_CASTER
                }
        }

        fun toDto() =
            when (this) {
                CASTER -> Dto.TargetingDto.CASTER
                ADJACENT_ENEMY -> Dto.TargetingDto.ADJACENT_ENEMY
                ALL_ADJACENT_ENEMIES -> Dto.TargetingDto.ALL_ADJACENT_ENEMIES
                VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT -> Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT
                VACANT_TILE_ADJACENT_TO_CASTER -> Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER
            }
    }

    data class Dto(
        val id: String,
        val manaCost: Int,
        val cooldown: Int,
        val targeting: TargetingDto,
        val abilityEffects: List<AbilityEffectDto>,
        val summonDuration: Int?,
    ) {
        enum class TargetingDto {
            CASTER,
            ADJACENT_ENEMY,
            ALL_ADJACENT_ENEMIES,
            VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT,
            VACANT_TILE_ADJACENT_TO_CASTER,
        }

        data class AbilityEffectDto(
            val effectId: String,
            val target: TargetExpressionDto,
        )

        data class TargetExpressionDto(
            val type: Type,
        ) {
            enum class Type {
                CASTER,
                TARGETED_BATTLE_UNIT,
                TARGETED_TILE,
                CASTER_TILE,
                ADJACENT_ALLIES,
            }
        }
    }
}
