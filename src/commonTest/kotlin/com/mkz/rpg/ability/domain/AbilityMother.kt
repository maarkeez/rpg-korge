package com.mkz.rpg.ability.domain

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.effect.domain.EffectMother.effectId
import korlibs.io.util.UUID
import kotlin.random.Random

object AbilityMother {
    fun ability(
        id: String = id(),
        manaCost: Int = manaCost(),
        cooldown: Int = cooldown(),
        abilityEffects: List<Ability.Dto.AbilityEffectDto> = abilityEffects(),
        targeting: Ability.Dto.TargetingDto = targeting(),
        summonDuration: Int? = null,
    ) = Ability.create(
        dto =
            Ability.Dto(
                id = id,
                manaCost = manaCost,
                cooldown = cooldown,
                abilityEffects = abilityEffects,
                targeting = targeting,
                summonDuration = summonDuration,
            ),
    )

    fun id() = "ability-${UUID.randomUUID()}"

    fun manaCost() = Random.nextInt(0, 999)

    fun cooldown() = Random.nextInt(0, 99)

    fun abilityEffects() = List(Random.nextInt(1, 4)) { abilityEffect() }

    fun abilityEffect(
        effectId: String = effectId(),
        target: TargetExpressionDto.Type = targetExpression(),
    ) = Ability.Dto.AbilityEffectDto(effectId = effectId, target = TargetExpressionDto(type = target))

    fun targetExpression() = TargetExpressionDto.Type.values().random()

    fun targeting() =
        Ability.Dto.TargetingDto
            .values()
            .random()
}
