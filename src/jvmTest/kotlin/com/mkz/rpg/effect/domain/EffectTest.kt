package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.ModifierDto
import com.mkz.rpg.effect.domain.Effect.Dto.ModifierDto.StackDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EffectTest {
    @Nested
    inner class Create {
        @Test
        fun `should create effect when the dto is valid`() {
            // Given
            val effectDto = EffectMother.effect().toDto()
            // When
            val createdEffect = Effect.create(effectDto)
            // Then
            assertThat(createdEffect.toDto()).isEqualTo(effectDto)
        }

        @Test
        fun `should fail when the effect id is blank`() {
            // Given
            val effectDto = EffectMother.effect().toDto().copy(id = "  ")
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.EmptyEffectId::class.java)
        }

        @Test
        fun `should fail when the power is negative`() {
            // Given
            val effectDto = EffectMother.effect().toDto().copy(power = -1)
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.NegativePower::class.java)
        }

        @Test
        fun `should fail when the power is above the limit`() {
            // Given
            val effectDto = EffectMother.effect().toDto().copy(power = 1000)
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.PowerAboveLimit::class.java)
        }

        @Test
        fun `should fail when the probability is negative`() {
            // Given
            val effectDto = EffectMother.effect().toDto().copy(probability = -1)
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.NegativeProbability::class.java)
        }

        @Test
        fun `should fail when the probability is above the limit`() {
            // Given
            val effectDto = EffectMother.effect().toDto().copy(probability = 101)
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.ProbabilityAboveLimit::class.java)
        }

        @Test
        fun `should fail when the modifier type is not supported`() {
            // Given
            val effectDto =
                EffectMother
                    .effect()
                    .toDto()
                    .copy(modifiers = listOf(ModifierDto(type = "UNKNOWN", stack = null)))
            // When
            val result = runCatching { Effect.create(effectDto) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(EffectError.InvalidEffectModifier::class.java)
        }

        @Test
        fun `should fail when the application type is not supported`() {
            // Given
            val effectDto =
                EffectMother
                    .effect()
                    .toDto()
                    .copy(application = ApplicationDto(type = "UNKNOWN", onTurnStarted = null, beforeApplyingEffect = null))
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
                    .effect()
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
                    .effect()
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
                    .effect()
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
            val effect = EffectMother.effect(id = "effect-1", power = 3)
            // When
            val result = effect.toDto()
            // Then
            assertThat(result.id).isEqualTo("effect-1")
            assertThat(result.type).isEqualTo(Effect.Dto.TypeDto.DECREASE_HEALTH)
            assertThat(result.power).isEqualTo(3)
            assertThat(result.probability).isEqualTo(100)
        }

        @Test
        fun `should expose the stack modifier as its dto representation`() {
            // Given
            val effectDto =
                EffectMother
                    .effect()
                    .toDto()
                    .copy(modifiers = listOf(ModifierDto(type = "STACK", stack = StackDto(maximum = 3))))
            val effect = Effect.create(effectDto)
            // When
            val result = effect.toDto()
            // Then
            assertThat(result.modifiers).containsExactly(ModifierDto(type = "STACK", stack = StackDto(maximum = 3)))
        }
    }

    @Nested
    inner class PullEvents {
        @Test
        fun `should pull the created event when the effect has pending events`() {
            // Given
            val createdEffect = Effect.create(EffectMother.effect().toDto())
            // When
            val (events, _) = createdEffect.pullEvents()
            // Then
            assertThat(events).containsExactly(EffectEvent.EffectCreated(effectId = createdEffect.toDto().id))
        }

        @Test
        fun `should not pull events when there are no pending events`() {
            // Given
            val effect = EffectMother.effect()
            // When
            val (events, _) = effect.pullEvents()
            // Then
            assertThat(events).isEmpty()
        }
    }
}
