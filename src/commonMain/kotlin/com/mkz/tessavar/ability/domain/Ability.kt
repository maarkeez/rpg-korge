package com.mkz.tessavar.ability.domain

import com.mkz.tessavar.ability.domain.AbilityError.AbilityCooldownAboveLimit
import com.mkz.tessavar.ability.domain.AbilityError.AbilityEffectsAboveLimit
import com.mkz.tessavar.ability.domain.AbilityError.AbilityEmptyEffects
import com.mkz.tessavar.ability.domain.AbilityError.AbilityManaCostAboveLimit
import com.mkz.tessavar.ability.domain.AbilityError.AbilitySummonDurationAboveLimit
import com.mkz.tessavar.ability.domain.AbilityError.AbilitySummonDurationBelowMinimum
import com.mkz.tessavar.ability.domain.AbilityError.EmptyAbilityId
import com.mkz.tessavar.ability.domain.AbilityError.NegativeAbilityCooldown
import com.mkz.tessavar.ability.domain.AbilityError.NegativeAbilityManaCost
import com.mkz.tessavar.ability.domain.AbilityEvent.AbilityCreated
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

    fun pullEvents(): Pair<Set<AbilityEvent>, Ability> = events to copy(events = emptySet())

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
            val target: EffectTarget,
        ) {
            constructor(dto: Dto.AbilityEffectDto) : this(EffectId(dto.effectId), EffectTarget(dto.target))

            fun toDto() = Dto.AbilityEffectDto(effectId = effectId.value, target = EffectTarget.of(target))

            @JvmInline private value class EffectId(
                val value: String,
            )
        }
    }

    sealed interface EffectTarget {
        data object Caster : EffectTarget

        data object TargetedBattleUnit : EffectTarget

        data object TargetedTile : EffectTarget

        data object CasterTile : EffectTarget

        data object AdjacentAllies : EffectTarget

        companion object {
            operator fun invoke(dto: Dto.EffectTargetDto): EffectTarget =
                when (dto.type) {
                    Dto.EffectTargetDto.Type.CASTER -> Caster
                    Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT -> TargetedBattleUnit
                    Dto.EffectTargetDto.Type.TARGETED_TILE -> TargetedTile
                    Dto.EffectTargetDto.Type.CASTER_TILE -> CasterTile
                    Dto.EffectTargetDto.Type.ADJACENT_ALLIES -> AdjacentAllies
                }

            fun of(effectTarget: EffectTarget): Dto.EffectTargetDto =
                Dto.EffectTargetDto(
                    type =
                        when (effectTarget) {
                            is Caster -> Dto.EffectTargetDto.Type.CASTER
                            is TargetedBattleUnit -> Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT
                            is TargetedTile -> Dto.EffectTargetDto.Type.TARGETED_TILE
                            is CasterTile -> Dto.EffectTargetDto.Type.CASTER_TILE
                            is AdjacentAllies -> Dto.EffectTargetDto.Type.ADJACENT_ALLIES
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
            val target: EffectTargetDto,
        )

        data class EffectTargetDto(
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
