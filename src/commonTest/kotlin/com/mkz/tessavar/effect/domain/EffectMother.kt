package com.mkz.tessavar.effect.domain

import com.mkz.tessavar.effect.domain.Effect.Dto.TimingDto
import com.mkz.tessavar.effect.domain.Effect.Dto.TimingDto.OnPlayerTurnStartedDto
import com.mkz.tessavar.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
import korlibs.io.util.UUID

object EffectMother {
    fun damageEffect(
        id: String = effectId(),
        damage: Int = 3,
        timingType: String = "ON_PLAYER_TURN_STARTED",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.damage(damage = damage),
                timing =
                    if (timingType == "IMMEDIATELY") {
                        TimingDto(
                            type = TimingTypeDto.IMMEDIATELY,
                            onPlayerTurnStarted = null,
                            beforeApplyingEffect = null,
                        )
                    } else {
                        TimingDto(
                            type = TimingTypeDto.ON_PLAYER_TURN_STARTED,
                            onPlayerTurnStarted =
                                OnPlayerTurnStartedDto(
                                    duration = 5,
                                ),
                            beforeApplyingEffect = null,
                        )
                    },
            ),
        ).pullEvents()
        .second

    fun healEffect(
        id: String = effectId(),
        healing: Int = 3,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.heal(healing = healing),
                timing =
                    TimingDto(
                        type = TimingTypeDto.IMMEDIATELY,
                        onPlayerTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun restoreManaEffect(
        id: String = effectId(),
        mana: Int = 3,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.restoreMana(mana = mana),
                timing =
                    TimingDto(
                        type = TimingTypeDto.IMMEDIATELY,
                        onPlayerTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun increaseMaximumHealthEffect(
        id: String = effectId(),
        maximumHealth: Int = 10,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.increaseMaximumHealth(maximumHealth = maximumHealth),
                timing = whileEquippedTiming(),
            ),
        ).pullEvents()
        .second

    fun increaseMaximumManaEffect(
        id: String = effectId(),
        maximumMana: Int = 10,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.increaseMaximumMana(maximumMana = maximumMana),
                timing = whileEquippedTiming(),
            ),
        ).pullEvents()
        .second

    fun whileEquippedTiming() =
        TimingDto(
            type = TimingTypeDto.WHILE_EQUIPPED,
            onPlayerTurnStarted = null,
            beforeApplyingEffect = null,
        )

    fun teleportEffect(id: String = effectId()) =
        Effect
            .create(
                Effect.Dto(
                    id = id,
                    outcome = Effect.Dto.EffectOutcomeDto.teleport(),
                    timing =
                        TimingDto(
                            type = TimingTypeDto.IMMEDIATELY,
                            onPlayerTurnStarted = null,
                            beforeApplyingEffect = null,
                        ),
                ),
            ).pullEvents()
            .second

    fun applyEffectToAdjacentAlliesEffect(
        id: String = effectId(),
        effectId: String = "effect-adjacent",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.applyEffectToAdjacentAllies(effectId = effectId),
                timing =
                    TimingDto(
                        type = TimingTypeDto.ON_DEFEATED,
                        onPlayerTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun summonEffect(
        id: String = effectId(),
        unitId: String = "unit-${UUID.randomUUID()}",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.summon(unitId = unitId),
                timing =
                    TimingDto(
                        type = TimingTypeDto.IMMEDIATELY,
                        onPlayerTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun learnAbilityEffect(
        id: String = effectId(),
        abilityId: String = "ability-${UUID.randomUUID()}",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.learnAbility(abilityId = abilityId),
                timing =
                    TimingDto(
                        type = TimingTypeDto.IMMEDIATELY,
                        onPlayerTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun effectId(): String = "effect-${UUID.randomUUID()}"
}
