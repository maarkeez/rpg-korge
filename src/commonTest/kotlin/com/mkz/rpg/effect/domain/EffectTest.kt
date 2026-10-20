package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.LEARN_ABILITY
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
        val effectDto = EffectMother.decreaseHealthEffect().toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect.toDto() shouldBe effectDto
    }

    @Test
    fun `Create should create effect when the application type is on defeated`() {
        // Given
        val effectDto =
            EffectMother
                .decreaseHealthEffect()
                .toDto()
                .copy(application = ApplicationDto(type = ApplicationTypeDto.ON_DEFEATED, onTurnStarted = null, beforeApplyingEffect = null))
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect.toDto() shouldBe effectDto
    }

    @Test
    fun `Create should fail when the effect id is blank`() {
        // Given
        val effectDto = EffectMother.decreaseHealthEffect().toDto().copy(id = "  ")
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
        val result = runCatching { EffectMother.decreaseHealthEffect(damage = damage) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativePower>()
    }

    @Test
    fun `Create should fail when the damage is above the limit`() {
        // Given
        val damage = 1000
        // When
        val result = runCatching { EffectMother.decreaseHealthEffect(damage = damage) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.PowerAboveLimit>()
    }

    @Test
    fun `Create should fail when the application is missing its details`() {
        // Given
        val effectDto =
            EffectMother
                .decreaseHealthEffect()
                .toDto()
                .copy(application = ApplicationDto(type = ApplicationTypeDto.ON_TURN_STARTED, onTurnStarted = null, beforeApplyingEffect = null))
        // When
        val result = runCatching { Effect.create(effectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.MissingEffectApplicationDetails>()
    }

    @Test
    fun `Create should fail when the application duration is negative`() {
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
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativeApplicationDuration>()
    }

    @Test
    fun `Create should fail when the application duration is above the limit`() {
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
        result.exceptionOrNull().shouldBeTypeOf<EffectError.ApplicationDurationAboveLimit>()
    }

    @Test
    fun `Create should create effect when the damage is zero`() {
        // Given
        val damage = 0
        // When
        val createdEffect = EffectMother.decreaseHealthEffect(damage = damage)
        // Then
        createdEffect
            .toDto()
            .outcome.decreaseHealth!!
            .damage shouldBe damage
    }

    @Test
    fun `Create should create effect when the damage is at the limit`() {
        // Given
        val damage = 999
        // When
        val createdEffect = EffectMother.decreaseHealthEffect(damage = damage)
        // Then
        createdEffect
            .toDto()
            .outcome.decreaseHealth!!
            .damage shouldBe damage
    }

    @Test
    fun `Create should create effect when the healing is zero`() {
        // Given
        val healing = 0
        // When
        val createdEffect = EffectMother.increaseHealthEffect(healing = healing)
        // Then
        createdEffect
            .toDto()
            .outcome.increaseHealth!!
            .healing shouldBe healing
    }

    @Test
    fun `Create should create effect when the healing is at the limit`() {
        // Given
        val healing = 999
        // When
        val createdEffect = EffectMother.increaseHealthEffect(healing = healing)
        // Then
        createdEffect
            .toDto()
            .outcome.increaseHealth!!
            .healing shouldBe healing
    }

    @Test
    fun `Create should create effect when the on turn started duration is zero`() {
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
        createdEffect
            .toDto()
            .application.onTurnStarted!!
            .duration shouldBe 0
    }

    @Test
    fun `Create should create effect when the on turn started duration is at the limit`() {
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
        createdEffect
            .toDto()
            .application.onTurnStarted!!
            .duration shouldBe 99
    }

    @Test
    fun `Create should create effect when the before applying effect duration is zero`() {
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
        createdEffect
            .toDto()
            .application.beforeApplyingEffect!!
            .duration shouldBe 0
    }

    @Test
    fun `Create should create effect when the before applying effect duration is at the limit`() {
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
        createdEffect
            .toDto()
            .application.beforeApplyingEffect!!
            .duration shouldBe 99
    }

    @Test
    fun `Create should fail when the before applying effect is missing its details`() {
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
        result.exceptionOrNull().shouldBeTypeOf<EffectError.MissingEffectApplicationDetails>()
    }

    @Test
    fun `Create should fail when the decrease health outcome is missing its details`() {
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
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the increase health outcome is missing its details`() {
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
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the apply effect on nearby allies outcome is missing its details`() {
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
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the deploy battle unit outcome is missing its details`() {
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
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should create effect when the outcome increases mana`() {
        // Given
        val effectDto = EffectMother.increaseManaEffect(mana = 999).toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .outcome.increaseMana!!
            .mana shouldBe 999
    }

    @Test
    fun `Create should create effect when the outcome increases the maximum health while equipped`() {
        // Given
        val effectDto = EffectMother.increaseMaximumHealthEffect(healthPoints = 0).toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect.toDto() shouldBe effectDto
        createdEffect.toDto().application.type shouldBe ApplicationTypeDto.WHILE_EQUIPPED
    }

    @Test
    fun `Create should create effect when the outcome increases the maximum mana while equipped`() {
        // Given
        val effectDto = EffectMother.increaseMaximumManaEffect(manaPoints = 999).toDto()
        // When
        val createdEffect = Effect.create(effectDto)
        // Then
        createdEffect
            .toDto()
            .outcome.increaseMaximumMana!!
            .manaPoints shouldBe 999
    }

    @Test
    fun `Create should fail when the mana is negative`() {
        // Given
        // When
        val result = runCatching { EffectMother.increaseManaEffect(mana = -1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativePower>()
    }

    @Test
    fun `Create should fail when the maximum health points are above the limit`() {
        // Given
        // When
        val result = runCatching { EffectMother.increaseMaximumHealthEffect(healthPoints = 1000) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.PowerAboveLimit>()
    }

    @Test
    fun `Create should fail when the maximum mana points are negative`() {
        // Given
        // When
        val result = runCatching { EffectMother.increaseMaximumManaEffect(manaPoints = -1) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.NegativePower>()
    }

    @Test
    fun `Create should fail when the increase mana outcome is missing its details`() {
        // Given
        val effectDto = EffectMother.increaseManaEffect().toDto()
        val invalidEffectDto = effectDto.copy(outcome = effectDto.outcome.copy(increaseMana = null))
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun `Create should fail when the outcome increases a maximum stat but is not applied while equipped`() {
        // Given
        val effectDto = EffectMother.increaseMaximumHealthEffect().toDto()
        val invalidEffectDto = effectDto.copy(application = EffectMother.increaseHealthEffect().toDto().application)
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.InvalidEffectApplication>()
    }

    @Test
    fun `Create should fail when the effect is applied while equipped but its outcome does not increase a maximum stat`() {
        // Given
        val invalidEffectDto = EffectMother.increaseManaEffect().toDto().copy(application = EffectMother.whileEquippedApplication())
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.InvalidEffectApplication>()
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
    fun `Create should fail when the outcome learns an ability but is not applied immediately`() {
        // Given
        val effectDto = EffectMother.learnAbilityEffect().toDto()
        val invalidEffectDto = effectDto.copy(application = EffectMother.decreaseHealthEffect().toDto().application)
        // When
        val result = runCatching { Effect.create(invalidEffectDto) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<EffectError.InvalidEffectApplication>()
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
        val effect = EffectMother.decreaseHealthEffect(id = "effect-1", damage = 3)
        // When
        val result = effect.toDto()
        // Then
        result.id shouldBe "effect-1"
        result.outcome.type shouldBe DECREASE_HEALTH
        result.outcome.decreaseHealth!!.damage shouldBe 3
    }

    @Test
    fun `ToDto should expose the apply effect on nearby allies outcome when converted to dto`() {
        // Given
        val effect = EffectMother.applyEffectOnNearbyAlliesEffect(effectId = "venom-damage")
        // When
        val result = effect.toDto()
        // Then
        result.outcome.type shouldBe APPLY_EFFECT_ON_NEARBY_ALLIES
        result.outcome.applyEffectOnNearbyAllies!!.effectId shouldBe "venom-damage"
    }

    @Test
    fun `ToDto should expose the deploy battle unit outcome when converted to dto`() {
        // Given
        val effect = EffectMother.deployBattleUnitEffect(unitId = "summoned-rat")
        // When
        val result = effect.toDto()
        // Then
        result.outcome.type shouldBe DEPLOY_BATTLE_UNIT
        result.outcome.deployBattleUnit!!.unitId shouldBe "summoned-rat"
    }

    @Test
    fun `ToDto should expose the destination when the effect application targets a tile`() {
        // Given
        val destination = Effect.Dto.EffectTargetDto.Tile(row = 2, column = 3)
        // When
        val effectApplication =
            Effect.Dto.EffectApplicationDto(
                source = ApplicationSourceDto.battleUnit("battle-unit-1"),
                target = Effect.Dto.EffectTargetDto.Unit(id = "battle-unit-2"),
                effectId = "effect-1",
                destination = destination,
            )
        // Then
        effectApplication.destination shouldBe destination
    }

    @Test
    fun `ToDto should expose the lootable item when the effect application comes from an item`() {
        // Given
        val source = ApplicationSourceDto.item("health-potion")
        // When
        val effectApplication =
            Effect.Dto.EffectApplicationDto(
                source = source,
                target = Effect.Dto.EffectTargetDto.Unit(id = "battle-unit-1"),
                effectId = "health-potion-heal",
            )
        // Then
        effectApplication.source.type shouldBe ApplicationSourceDto.SourceTypeDto.ITEM
        effectApplication.source.lootableItemId shouldBe "health-potion"
        effectApplication.source.battleUnitId.shouldBeNull()
    }

    @Test
    fun `ToDto should expose the before applying effect details when the application type is before applying effect`() {
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
        application.beforeApplyingEffect shouldBe beforeApplyingEffect
        application.onTurnStarted.shouldBeNull()
    }

    @Test
    fun `PullEvents should pull the created event when the effect has pending events`() {
        // Given
        val createdEffect = Effect.create(EffectMother.decreaseHealthEffect().toDto())
        // When
        val (events, _) = createdEffect.pullEvents()
        // Then
        events.shouldContainExactly(EffectEvent.EffectCreated(effectId = createdEffect.toDto().id))
    }

    @Test
    fun `PullEvents should not pull events when there are no pending events`() {
        // Given
        val effect = EffectMother.decreaseHealthEffect()
        // When
        val (events, _) = effect.pullEvents()
        // Then
        events.shouldBeEmpty()
    }
}
