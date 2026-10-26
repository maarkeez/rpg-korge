package com.mkz.rpg.ability.domain

import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.effect.domain.EffectMother.effectId
import korlibs.io.util.UUID
import kotlin.random.Random

object AbilityMother {
    fun ability(
        id: String = id(),
        cost: Int = cost(),
        cooldown: Int = cooldown(),
        effectSpecs: List<Ability.Dto.EffectSpecDto> = effectSpecs(),
        targeting: Ability.Dto.TargetingDto = targeting(),
        turnsDuration: Int? = null,
    ) = Ability.create(
        dto =
            Ability.Dto(
                id = id,
                cost = cost,
                cooldown = cooldown,
                effectSpecs = effectSpecs,
                targeting = targeting,
                turnsDuration = turnsDuration,
            ),
    )

    fun id() = "ability-${UUID.randomUUID()}"

    fun cost() = Random.nextInt(0, 999)

    fun cooldown() = Random.nextInt(0, 99)

    fun effectSpecs() = List(Random.nextInt(1, 4)) { effectSpec() }

    fun effectSpec(
        effectId: String = effectId(),
        target: TargetExpressionDto.Type = targetExpression(),
    ) = Ability.Dto.EffectSpecDto(effectId = effectId, target = TargetExpressionDto(type = target))

    fun targetExpression() = TargetExpressionDto.Type.values().random()

    fun targeting() =
        Ability.Dto.TargetingDto
            .values()
            .random()
}
