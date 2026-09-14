package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
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
        fun `should fail when the application type is not supported`() {
            // Given
            val applicationType = "UNKNOWN"
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(application = ApplicationDto(type = applicationType, onTurnStarted = null, beforeApplyingEffect = null))
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.InvalidEffectApplication::class.java)
        }

        @Test
        fun `should fail when the application is missing its details`() {
            // Given
            val effectDto =
                EffectMother
                    .decreaseHealthEffect()
                    .toDto()
                    .copy(application = ApplicationDto(type = "ON_TURN_STARTED", onTurnStarted = null, beforeApplyingEffect = null))
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
                                type = "ON_TURN_STARTED",
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
                                type = "ON_TURN_STARTED",
                                onTurnStarted = OnTurnStartedDto(duration = 100),
                                beforeApplyingEffect = null,
                            ),
                    )
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.ApplicationDurationAboveLimit::class.java)
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
