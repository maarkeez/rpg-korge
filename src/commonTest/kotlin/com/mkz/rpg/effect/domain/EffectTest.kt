package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_TO_ADJACENT_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DAMAGE
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.LEARN_ABILITY
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.SUMMON
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto.OnPlayerTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class EffectTest {
    @Test
    fun `Create should create effect when the dto is valid`() {
        // Given
        val effectDto = EffectMother.damageEffect().toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect.toDto() shouldBe effectDto
    }

    @Test
    fun `Create should create effect when the timing is on defeated`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(timing = TimingDto(type = TimingTypeDto.ON_DEFEATED, onPlayerTurnStarted = null, beforeApplyingEffect = null))
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect.toDto() shouldBe effectDto
    }

    @Test
    fun `Create should fail when the effect id is blank`() {
        // Given
        val effectDto = EffectMother.damageEffect().toDto().copy(id = "  ")
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.EmptyEffectId>()
    }

    @Test
    fun `Create should fail when the damage is negative`() {
        // Given
        val damage = -1
        // When
        val result = runCatching { EffectMother.damageEffect(damage = damage) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativeAmount>()
    }

    @Test
    fun `Create should fail when the damage is above the limit`() {
        // Given
        val damage = 1000
        // When
        val result = runCatching { EffectMother.damageEffect(damage = damage) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.AmountAboveLimit>()
    }

    @Test
    fun `Create should fail when the timing is missing its details`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(timing = TimingDto(type = TimingTypeDto.ON_PLAYER_TURN_STARTED, onPlayerTurnStarted = null, beforeApplyingEffect = null))
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.MissingEffectTimingDetails>()
    }

    @Test
    fun `Create should fail when the duration is negative`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    timing =
                        TimingDto(
                            type = TimingTypeDto.ON_PLAYER_TURN_STARTED,
                            onPlayerTurnStarted = OnPlayerTurnStartedDto(duration = -1),
                            beforeApplyingEffect = null,
                        ),
                )
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativeDuration>()
    }

    @Test
    fun `Create should fail when the duration is above the limit`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    timing =
                        TimingDto(
                            type = TimingTypeDto.ON_PLAYER_TURN_STARTED,
                            onPlayerTurnStarted = OnPlayerTurnStartedDto(duration = 100),
                            beforeApplyingEffect = null,
                        ),
                )
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.DurationAboveLimit>()
    }

    @Test
    fun `Create should create effect when the damage is zero`() {
        // Given
        val damage = 0
        // When
        val createdEffect = EffectMother.damageEffect(damage = damage)
        // Then
        createdEffect
            .toDto()
            .outcome.damage!!
            .damage shouldBe damage
    }

    @Test
    fun `Create should create effect when the damage is at the limit`() {
        // Given
        val damage = 999
        // When
        val createdEffect = EffectMother.damageEffect(damage = damage)
        // Then
        createdEffect
            .toDto()
            .outcome.damage!!
            .damage shouldBe damage
    }

    @Test
    fun `Create should create effect when the healing is zero`() {
        // Given
        val healing = 0
        // When
        val createdEffect = EffectMother.healEffect(healing = healing)
        // Then
        createdEffect
            .toDto()
            .outcome.heal!!
            .healing shouldBe healing
    }

    @Test
    fun `Create should create effect when the healing is at the limit`() {
        // Given
        val healing = 999
        // When
        val createdEffect = EffectMother.healEffect(healing = healing)
        // Then
        createdEffect
            .toDto()
            .outcome.heal!!
            .healing shouldBe healing
    }

    @Test
    fun `Create should create effect when the on player turn started duration is zero`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    timing =
                        TimingDto(
                            type = TimingTypeDto.ON_PLAYER_TURN_STARTED,
                            onPlayerTurnStarted = OnPlayerTurnStartedDto(duration = 0),
                            beforeApplyingEffect = null,
                        ),
                )
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .timing.onPlayerTurnStarted!!
            .duration shouldBe 0
    }

    @Test
    fun `Create should create effect when the on player turn started duration is at the limit`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    timing =
                        TimingDto(
                            type = TimingTypeDto.ON_PLAYER_TURN_STARTED,
                            onPlayerTurnStarted = OnPlayerTurnStartedDto(duration = 99),
                            beforeApplyingEffect = null,
                        ),
                )
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .timing.onPlayerTurnStarted!!
            .duration shouldBe 99
    }

    @Test
    fun `Create should create effect when the before applying effect duration is zero`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    timing =
                        TimingDto(
                            type = TimingTypeDto.BEFORE_APPLYING_EFFECT,
                            onPlayerTurnStarted = null,
                            beforeApplyingEffect = BeforeApplyingEffectDto(duration = 0),
                        ),
                )
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .timing.beforeApplyingEffect!!
            .duration shouldBe 0
    }

    @Test
    fun `Create should create effect when the before applying effect duration is at the limit`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    timing =
                        TimingDto(
                            type = TimingTypeDto.BEFORE_APPLYING_EFFECT,
                            onPlayerTurnStarted = null,
                            beforeApplyingEffect = BeforeApplyingEffectDto(duration = 99),
                        ),
                )
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .timing.beforeApplyingEffect!!
            .duration shouldBe 99
    }

    @Test
    fun `Create should fail when the before applying effect is missing its details`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    timing =
                        TimingDto(
                            type = TimingTypeDto.BEFORE_APPLYING_EFFECT,
                            onPlayerTurnStarted = null,
                            beforeApplyingEffect = null,
                        ),
                )
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.MissingEffectTimingDetails>()
    }

    @Test
    fun `Create should fail when the damage outcome is missing its details`() {
        // Given
        val effectDto =
            EffectMother
                .damageEffect()
                .toDto()
                .copy(
                    outcome =
                        EffectMother
                            .damageEffect()
                            .toDto()
                            .outcome
                            .copy(damage = null),
                )
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the heal outcome is missing its details`() {
        // Given
        val effectDto =
            EffectMother
                .healEffect()
                .toDto()
                .copy(
                    outcome =
                        EffectMother
                            .healEffect()
                            .toDto()
                            .outcome
                            .copy(heal = null),
                )
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the apply effect to adjacent allies outcome is missing its details`() {
        // Given
        val effectDto =
            EffectMother
                .applyEffectToAdjacentAlliesEffect()
                .toDto()
                .copy(
                    outcome =
                        EffectMother
                            .applyEffectToAdjacentAlliesEffect()
                            .toDto()
                            .outcome
                            .copy(applyEffectToAdjacentAllies = null),
                )
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the deploy battle unit outcome is missing its details`() {
        // Given
        val effectDto =
            EffectMother
                .summonEffect()
                .toDto()
                .copy(
                    outcome =
                        EffectMother
                            .summonEffect()
                            .toDto()
                            .outcome
                            .copy(summon = null),
                )
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should create effect when the outcome restores mana`() {
        // Given
        val effectDto = EffectMother.restoreManaEffect(mana = 999).toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .outcome.restoreMana!!
            .mana shouldBe 999
    }

    @Test
    fun `Create should create effect when the outcome increases the maximum health while equipped`() {
        // Given
        val effectDto = EffectMother.increaseMaximumHealthEffect(maximumHealth = 0).toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect.toDto() shouldBe effectDto
        createdEffect.toDto().timing.type shouldBe TimingTypeDto.WHILE_EQUIPPED
    }

    @Test
    fun `Create should create effect when the outcome increases the maximum mana while equipped`() {
        // Given
        val effectDto = EffectMother.increaseMaximumManaEffect(maximumMana = 999).toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .outcome.increaseMaximumMana!!
            .maximumMana shouldBe 999
    }

    @Test
    fun `Create should fail when the mana is negative`() {
        // Given
        // When
        val result = runCatching { EffectMother.restoreManaEffect(mana = -1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativeAmount>()
    }

    @Test
    fun `Create should fail when the maximum health points are above the limit`() {
        // Given
        // When
        val result = runCatching { EffectMother.increaseMaximumHealthEffect(maximumHealth = 1000) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.AmountAboveLimit>()
    }

    @Test
    fun `Create should fail when the maximum mana points are negative`() {
        // Given
        // When
        val result = runCatching { EffectMother.increaseMaximumManaEffect(maximumMana = -1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativeAmount>()
    }

    @Test
    fun `Create should fail when the restore mana outcome is missing its details`() {
        // Given
        val effectDto = EffectMother.restoreManaEffect().toDto()
        val invalidEffectDto = effectDto.copy(outcome = effectDto.outcome.copy(restoreMana = null))
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the outcome increases a maximum stat but is not timed while equipped`() {
        // Given
        val effectDto = EffectMother.increaseMaximumHealthEffect().toDto()
        val invalidEffectDto = effectDto.copy(timing = EffectMother.healEffect().toDto().timing)
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.InvalidEffectTiming>()
    }

    @Test
    fun `Create should fail when the effect is timed while equipped but its outcome does not increase a maximum stat`() {
        // Given
        val invalidEffectDto = EffectMother.restoreManaEffect().toDto().copy(timing = EffectMother.whileEquippedTiming())
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.InvalidEffectTiming>()
    }

    @Test
    fun `Create should create effect when the outcome learns an ability immediately`() {
        // Given
        val effectDto = EffectMother.learnAbilityEffect(abilityId = "teleport").toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect.toDto() shouldBe effectDto
    }

    @Test
    fun `Create should fail when the outcome learns an ability but is not timed immediately`() {
        // Given
        val effectDto = EffectMother.learnAbilityEffect().toDto()
        val invalidEffectDto = effectDto.copy(timing = EffectMother.damageEffect().toDto().timing)
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.InvalidEffectTiming>()
    }

    @Test
    fun `Create should fail when the learn ability outcome is missing its details`() {
        // Given
        val effectDto = EffectMother.learnAbilityEffect().toDto()
        val invalidEffectDto = effectDto.copy(outcome = effectDto.outcome.copy(learnAbility = null))
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `ToDto should expose the learn ability outcome when converted to dto`() {
        // Given
        val effect = EffectMother.learnAbilityEffect(abilityId = "teleport")
        // When
        val result = effect.toDto()
        // Then
        result.outcome.type shouldBe LEARN_ABILITY
        result.outcome.learnAbility!!.abilityId shouldBe "teleport"
    }

    @Test
    fun `ToDto should expose the effect data when converted to dto`() {
        // Given
        val effect = EffectMother.damageEffect(id = "effect-1", damage = 3)
        // When
        val result = effect.toDto()
        // Then
        result.id shouldBe "effect-1"
        result.outcome.type shouldBe DAMAGE
        result.outcome.damage!!.damage shouldBe 3
    }

    @Test
    fun `ToDto should expose the apply effect to adjacent allies outcome when converted to dto`() {
        // Given
        val effect = EffectMother.applyEffectToAdjacentAlliesEffect(effectId = "venom-damage")
        // When
        val result = effect.toDto()
        // Then
        result.outcome.type shouldBe APPLY_EFFECT_TO_ADJACENT_ALLIES
        result.outcome.applyEffectToAdjacentAllies!!.effectId shouldBe "venom-damage"
    }

    @Test
    fun `ToDto should expose the deploy battle unit outcome when converted to dto`() {
        // Given
        val effect = EffectMother.summonEffect(unitId = "summoned-rat")
        // When
        val result = effect.toDto()
        // Then
        result.outcome.type shouldBe SUMMON
        result.outcome.summon!!.unitId shouldBe "summoned-rat"
    }

    @Test
    fun `ToDto should expose the destination when the effect application targets a tile`() {
        // Given
        val destination = Effect.Dto.RecipientDto.position(row = 2, column = 3)
        // When
        val effectApplication =
            Effect.Dto.EffectApplicationDto(
                source = SourceDto.battleUnit("battle-unit-1"),
                recipient = Effect.Dto.RecipientDto.battleUnit("battle-unit-2"),
                effectId = "effect-1",
                destination = destination,
            )
        // Then
        effectApplication.destination shouldBe destination
    }

    @Test
    fun `ToDto should expose the lootable item when the effect application comes from an item`() {
        // Given
        val source = SourceDto.item("health-potion")
        // When
        val effectApplication =
            Effect.Dto.EffectApplicationDto(
                source = source,
                recipient = Effect.Dto.RecipientDto.battleUnit("battle-unit-1"),
                effectId = "health-potion-heal",
                destination = null,
            )
        // Then
        effectApplication.source.type shouldBe SourceDto.SourceTypeDto.ITEM
        effectApplication.source.lootableItemId shouldBe "health-potion"
        effectApplication.source.battleUnitId.shouldBeNull()
    }

    @Test
    fun `ToDto should expose the before applying effect details when the timing is before applying effect`() {
        // Given
        val beforeApplyingEffect = BeforeApplyingEffectDto(duration = 3)
        // When
        val timing =
            TimingDto(
                type = TimingTypeDto.BEFORE_APPLYING_EFFECT,
                onPlayerTurnStarted = null,
                beforeApplyingEffect = beforeApplyingEffect,
            )
        // Then
        timing.beforeApplyingEffect shouldBe beforeApplyingEffect
        timing.onPlayerTurnStarted.shouldBeNull()
    }

    @Test
    fun `PullEvents should pull the created event when the effect has pending events`() {
        // Given
        val createdEffect = Effect.create(EffectMother.damageEffect().toDto())
        // When
        val (events, _) = createdEffect.pullEvents()
        // Then
        events.shouldContainExactly(EffectEvent.EffectCreated(effectId = createdEffect.toDto().id))
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val effect = EffectMother.damageEffect()
        // When
        val (events, _) = effect.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
