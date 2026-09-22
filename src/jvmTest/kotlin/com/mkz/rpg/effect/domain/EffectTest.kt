package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EffectTest {
    @Nested
    inner class Create {
        @Test
        fun `should create effect when the dto is valid`() {
            // Given
            val effectDto = EffectMother.decreaseHealthEffect().toDto()
            // When
            val createdEffect = Effect.create(effectDto)
            // Then
            assertThat(createdEffect.toDto()).isEqualTo(effectDto)
        }

        @Test
        fun `should create effect when the application type is on defeated`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(application = ApplicationDto(type = ApplicationTypeDto.ON_DEFEATED, onTurnStarted = null, beforeApplyingEffect = null))
            // When
            val createdEffect = Effect.create(effectDto)
            // Then
            assertThat(createdEffect.toDto()).isEqualTo(effectDto)
        }

        @Test
        fun `should fail when the effect id is blank`() {
            // Given
            val effectDto = EffectMother.decreaseHealthEffect().toDto().copy(id = "  ")
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.EmptyEffectId::class.java)
        }

        @Test
        fun `should fail when the damage is negative`() {
            // Given
            val damage = -1
            // When
            val result = runCatching { EffectMother.decreaseHealthEffect(damage = damage) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.NegativePower::class.java)
        }

        @Test
        fun `should fail when the damage is above the limit`() {
            // Given
            val damage = 1000
            // When
            val result = runCatching { EffectMother.decreaseHealthEffect(damage = damage) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.PowerAboveLimit::class.java)
        }

        @Test
        fun `should fail when the application is missing its details`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(application = ApplicationDto(type = ApplicationTypeDto.ON_TURN_STARTED, onTurnStarted = null, beforeApplyingEffect = null))
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.MissingEffectApplicationDetails::class.java)
        }

        @Test
        fun `should fail when the application duration is negative`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        application =
                            ApplicationDto(
                                type = ApplicationTypeDto.ON_TURN_STARTED,
                                onTurnStarted = OnTurnStartedDto(duration = -1),
                                beforeApplyingEffect = null,
                            ),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.NegativeApplicationDuration::class.java)
        }

        @Test
        fun `should fail when the application duration is above the limit`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        application =
                            ApplicationDto(
                                type = ApplicationTypeDto.ON_TURN_STARTED,
                                onTurnStarted = OnTurnStartedDto(duration = 100),
                                beforeApplyingEffect = null,
                            ),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.ApplicationDurationAboveLimit::class.java)
        }

        @Test
        fun `should create effect when the damage is zero`() {
            // Given
            val damage = 0
            // When
            val createdEffect = EffectMother.decreaseHealthEffect(damage = damage)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .outcome.decreaseHealth!!
                    .damage,
            ).isEqualTo(damage)
        }

        @Test
        fun `should create effect when the damage is at the limit`() {
            // Given
            val damage = 999
            // When
            val createdEffect = EffectMother.decreaseHealthEffect(damage = damage)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .outcome.decreaseHealth!!
                    .damage,
            ).isEqualTo(damage)
        }

        @Test
        fun `should create effect when the healing is zero`() {
            // Given
            val healing = 0
            // When
            val createdEffect = EffectMother.increaseHealthEffect(healing = healing)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .outcome.increaseHealth!!
                    .healing,
            ).isEqualTo(healing)
        }

        @Test
        fun `should create effect when the healing is at the limit`() {
            // Given
            val healing = 999
            // When
            val createdEffect = EffectMother.increaseHealthEffect(healing = healing)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .outcome.increaseHealth!!
                    .healing,
            ).isEqualTo(healing)
        }

        @Test
        fun `should create effect when the on turn started duration is zero`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        application =
                            ApplicationDto(
                                type = ApplicationTypeDto.ON_TURN_STARTED,
                                onTurnStarted = OnTurnStartedDto(duration = 0),
                                beforeApplyingEffect = null,
                            ),
                    )
            // When
            val createdEffect = Effect.create(effectDto)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .application.onTurnStarted!!
                    .duration,
            ).isEqualTo(0)
        }

        @Test
        fun `should create effect when the on turn started duration is at the limit`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        application =
                            ApplicationDto(
                                type = ApplicationTypeDto.ON_TURN_STARTED,
                                onTurnStarted = OnTurnStartedDto(duration = 99),
                                beforeApplyingEffect = null,
                            ),
                    )
            // When
            val createdEffect = Effect.create(effectDto)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .application.onTurnStarted!!
                    .duration,
            ).isEqualTo(99)
        }

        @Test
        fun `should create effect when the before applying effect duration is zero`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        application =
                            ApplicationDto(
                                type = ApplicationTypeDto.BEFORE_APPLYING_EFFECT,
                                onTurnStarted = null,
                                beforeApplyingEffect = BeforeApplyingEffectDto(duration = 0),
                            ),
                    )
            // When
            val createdEffect = Effect.create(effectDto)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .application.beforeApplyingEffect!!
                    .duration,
            ).isEqualTo(0)
        }

        @Test
        fun `should create effect when the before applying effect duration is at the limit`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        application =
                            ApplicationDto(
                                type = ApplicationTypeDto.BEFORE_APPLYING_EFFECT,
                                onTurnStarted = null,
                                beforeApplyingEffect = BeforeApplyingEffectDto(duration = 99),
                            ),
                    )
            // When
            val createdEffect = Effect.create(effectDto)
            // Then
            assertThat(
                createdEffect
                    .toDto()
                    .application.beforeApplyingEffect!!
                    .duration,
            ).isEqualTo(99)
        }

        @Test
        fun `should fail when the before applying effect is missing its details`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        application =
                            ApplicationDto(
                                type = ApplicationTypeDto.BEFORE_APPLYING_EFFECT,
                                onTurnStarted = null,
                                beforeApplyingEffect = null,
                            ),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.MissingEffectApplicationDetails::class.java)
        }

        @Test
        fun `should fail when the decrease health outcome is missing its details`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(
                        outcome =
                            EffectMother
                                .decreaseHealthEffect()
                                .toDto()
                                .outcome
                                .copy(decreaseHealth = null),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isInstanceOf(NullPointerException::class.java)
        }

        @Test
        fun `should fail when the increase health outcome is missing its details`() {
            // Given
            val effectDto =
                EffectMother
                    .increaseHealthEffect()
                    .toDto()
                    .copy(
                        outcome =
                            EffectMother
                                .increaseHealthEffect()
                                .toDto()
                                .outcome
                                .copy(increaseHealth = null),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isInstanceOf(NullPointerException::class.java)
        }

        @Test
        fun `should fail when the apply effect on nearby allies outcome is missing its details`() {
            // Given
            val effectDto =
                EffectMother
                    .applyEffectOnNearbyAlliesEffect()
                    .toDto()
                    .copy(
                        outcome =
                            EffectMother
                                .applyEffectOnNearbyAlliesEffect()
                                .toDto()
                                .outcome
                                .copy(applyEffectOnNearbyAllies = null),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isInstanceOf(NullPointerException::class.java)
        }

        @Test
        fun `should fail when the deploy battle unit outcome is missing its details`() {
            // Given
            val effectDto =
                EffectMother
                    .deployBattleUnitEffect()
                    .toDto()
                    .copy(
                        outcome =
                            EffectMother
                                .deployBattleUnitEffect()
                                .toDto()
                                .outcome
                                .copy(deployBattleUnit = null),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isInstanceOf(NullPointerException::class.java)
        }
    }

    @Nested
    inner class ToDto {
        @Test
        fun `should expose the effect data when converted to dto`() {
            // Given
            val effect = EffectMother.decreaseHealthEffect(id = "effect-1", damage = 3)
            // When
            val result = effect.toDto()
            // Then
            assertThat(result.id).isEqualTo("effect-1")
            assertThat(result.outcome.type).isEqualTo(DECREASE_HEALTH)
            assertThat(result.outcome.decreaseHealth!!.damage).isEqualTo(3)
        }

        @Test
        fun `should expose the apply effect on nearby allies outcome when converted to dto`() {
            // Given
            val effect = EffectMother.applyEffectOnNearbyAlliesEffect(effectId = "venom-damage")
            // When
            val result = effect.toDto()
            // Then
            assertThat(result.outcome.type).isEqualTo(APPLY_EFFECT_ON_NEARBY_ALLIES)
            assertThat(result.outcome.applyEffectOnNearbyAllies!!.effectId).isEqualTo("venom-damage")
        }

        @Test
        fun `should expose the deploy battle unit outcome when converted to dto`() {
            // Given
            val effect = EffectMother.deployBattleUnitEffect(unitId = "summoned-rat")
            // When
            val result = effect.toDto()
            // Then
            assertThat(result.outcome.type).isEqualTo(DEPLOY_BATTLE_UNIT)
            assertThat(result.outcome.deployBattleUnit!!.unitId).isEqualTo("summoned-rat")
        }

        @Test
        fun `should expose the destination when the effect application targets a tile`() {
            // Given
            val destination = Effect.Dto.EffectTargetDto.Tile(row = 2, column = 3)
            // When
            val effectApplication =
                Effect.Dto.EffectApplicationDto(
                    source = "battle-unit-1",
                    target = Effect.Dto.EffectTargetDto.Unit(id = "battle-unit-2"),
                    effectId = "effect-1",
                    destination = destination,
                )
            // Then
            assertThat(effectApplication.destination).isEqualTo(destination)
        }

        @Test
        fun `should expose the before applying effect details when the application type is before applying effect`() {
            // Given
            val beforeApplyingEffect = BeforeApplyingEffectDto(duration = 3)
            // When
            val application =
                ApplicationDto(
                    type = ApplicationTypeDto.BEFORE_APPLYING_EFFECT,
                    onTurnStarted = null,
                    beforeApplyingEffect = beforeApplyingEffect,
                )
            // Then
            assertThat(application.beforeApplyingEffect).isEqualTo(beforeApplyingEffect)
            assertThat(application.onTurnStarted).isNull()
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the created event when the effect has pending events`() {
            // Given
            val createdEffect = Effect.create(EffectMother.decreaseHealthEffect().toDto())
            // When
            val (events, _) = createdEffect.pullEvents()
            // Then
            assertThat(events).containsExactly(EffectEvent.EffectCreated(effectId = createdEffect.toDto().id))
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val effect = EffectMother.decreaseHealthEffect()
            // When
            val (events, _) = effect.pullEvents()
            // Then
            assertThat(events).isEmpty()
        }
    }
}
