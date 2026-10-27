package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MANA
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MAXIMUM_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MAXIMUM_MANA
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.LEARN_ABILITY
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.NEGATE_INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.EffectError.ApplicationDurationAboveLimit
import com.mkz.rpg.effect.domain.EffectError.EmptyEffectId
import com.mkz.rpg.effect.domain.EffectError.InvalidEffectApplication
import com.mkz.rpg.effect.domain.EffectError.MissingEffectApplicationDetails
import com.mkz.rpg.effect.domain.EffectError.NegativeApplicationDuration
import com.mkz.rpg.effect.domain.EffectError.NegativePower
import com.mkz.rpg.effect.domain.EffectError.PowerAboveLimit
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Effect private constructor(
    private val id: Id,
    private val outcome: Outcome,
    private val application: Application,
    private val events: Set<EffectEvent>,
) {
    companion object {
        /**
         * Fails when the outcome only makes sense while an item is equipped but the application is not, or the other way round,
         * and when learning an ability is not applied immediately.
         */
        fun create(dto: Dto): Effect {
            val outcome = Outcome(dto.outcome)
            val application = Application(dto.application)
            if (outcome.lastsWhileEquipped() != application.isWhileEquipped()) throw InvalidEffectApplication()
            if (outcome.learnsAbility() && !application.isImmediately()) throw InvalidEffectApplication()
            return Effect(
                id = Id(dto.id),
                outcome = outcome,
                application = application,
                events = setOf(EffectEvent.EffectCreated(dto.id)),
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            outcome = outcome.toDto(),
            application = application.toDto(),
        )

    @JvmInline private value class Id(
        val value: String,
    ) {
        init {
            if (value.isBlank()) throw EmptyEffectId()
        }
    }

    private sealed interface Outcome {
        companion object {
            operator fun invoke(dto: Dto.EffectOutcomeDto): Outcome =
                when (dto.type) {
                    DECREASE_HEALTH -> DecreaseHealth(dto.decreaseHealth!!.damage)
                    INCREASE_HEALTH -> IncreaseHealth(dto.increaseHealth!!.healing)
                    NEGATE_INCREASE_HEALTH -> NegateIncreaseHealth
                    TELEPORT -> Teleport
                    APPLY_EFFECT_ON_NEARBY_ALLIES -> ApplyEffectOnNearbyAllies(dto.applyEffectOnNearbyAllies!!.effectId)
                    DEPLOY_BATTLE_UNIT -> DeployBattleUnit(dto.deployBattleUnit!!.unitId)
                    INCREASE_MANA -> IncreaseMana(dto.increaseMana!!.mana)
                    INCREASE_MAXIMUM_HEALTH -> IncreaseMaximumHealth(dto.increaseMaximumHealth!!.healthPoints)
                    INCREASE_MAXIMUM_MANA -> IncreaseMaximumMana(dto.increaseMaximumMana!!.manaPoints)
                    LEARN_ABILITY -> LearnAbility(dto.learnAbility!!.abilityId)
                }
        }

        /** Raising a maximum stat only lasts while the item granting it is equipped. */
        fun lastsWhileEquipped() = this is IncreaseMaximumHealth || this is IncreaseMaximumMana

        /** Learning an ability happens once, as soon as it is applied. */
        fun learnsAbility() = this is LearnAbility

        fun toDto() =
            when (this) {
                is DecreaseHealth -> Dto.EffectOutcomeDto.decreaseHealth(damage = damage)
                is IncreaseHealth -> Dto.EffectOutcomeDto.increaseHealth(healing = healing)
                NegateIncreaseHealth -> Dto.EffectOutcomeDto.negateIncreaseHealth()
                Teleport -> Dto.EffectOutcomeDto.teleport()
                is ApplyEffectOnNearbyAllies -> Dto.EffectOutcomeDto.applyEffectOnNearbyAllies(effectId = effectId)
                is DeployBattleUnit -> Dto.EffectOutcomeDto.deployBattleUnit(unitId = unitId)
                is IncreaseMana -> Dto.EffectOutcomeDto.increaseMana(mana = mana)
                is IncreaseMaximumHealth -> Dto.EffectOutcomeDto.increaseMaximumHealth(healthPoints = healthPoints)
                is IncreaseMaximumMana -> Dto.EffectOutcomeDto.increaseMaximumMana(manaPoints = manaPoints)
                is LearnAbility -> Dto.EffectOutcomeDto.learnAbility(abilityId = abilityId)
            }

        @JvmInline private value class DecreaseHealth(
            val damage: Int,
        ) : Outcome {
            init {
                if (damage < 0) throw NegativePower()
                if (damage > 999) throw PowerAboveLimit()
            }
        }

        @JvmInline private value class IncreaseHealth(
            val healing: Int,
        ) : Outcome {
            init {
                if (healing < 0) throw NegativePower()
                if (healing > 999) throw PowerAboveLimit()
            }
        }

        @JvmInline private value class IncreaseMana(
            val mana: Int,
        ) : Outcome {
            init {
                if (mana < 0) throw NegativePower()
                if (mana > 999) throw PowerAboveLimit()
            }
        }

        @JvmInline private value class IncreaseMaximumHealth(
            val healthPoints: Int,
        ) : Outcome {
            init {
                if (healthPoints < 0) throw NegativePower()
                if (healthPoints > 999) throw PowerAboveLimit()
            }
        }

        @JvmInline private value class IncreaseMaximumMana(
            val manaPoints: Int,
        ) : Outcome {
            init {
                if (manaPoints < 0) throw NegativePower()
                if (manaPoints > 999) throw PowerAboveLimit()
            }
        }

        @JvmInline private value class ApplyEffectOnNearbyAllies(
            val effectId: String,
        ) : Outcome

        @JvmInline private value class DeployBattleUnit(
            val unitId: String,
        ) : Outcome

        /** Teaches its battle unit the ability for good. */
        @JvmInline private value class LearnAbility(
            val abilityId: String,
        ) : Outcome

        private object NegateIncreaseHealth : Outcome

        private object Teleport : Outcome
    }

    private sealed interface Application {
        companion object {
            operator fun invoke(dto: ApplicationDto): Application =
                when (dto.type) {
                    ApplicationTypeDto.IMMEDIATELY -> Immediately
                    ApplicationTypeDto.ON_TURN_STARTED -> OnTurnStarted(dto.onTurnStarted ?: throw MissingEffectApplicationDetails())
                    ApplicationTypeDto.BEFORE_APPLYING_EFFECT -> BeforeApplyingEffect(dto.beforeApplyingEffect ?: throw MissingEffectApplicationDetails())
                    ApplicationTypeDto.ON_DEFEATED -> OnDefeated
                    ApplicationTypeDto.WHILE_EQUIPPED -> WhileEquipped
                }
        }

        fun isWhileEquipped() = this is WhileEquipped

        fun isImmediately() = this is Immediately

        fun toDto() =
            ApplicationDto(
                type =
                    when (this) {
                        is Immediately -> ApplicationTypeDto.IMMEDIATELY
                        is OnTurnStarted -> ApplicationTypeDto.ON_TURN_STARTED
                        is BeforeApplyingEffect -> ApplicationTypeDto.BEFORE_APPLYING_EFFECT
                        is OnDefeated -> ApplicationTypeDto.ON_DEFEATED
                        is WhileEquipped -> ApplicationTypeDto.WHILE_EQUIPPED
                    },
                onTurnStarted = if (this is OnTurnStarted) toTurnStartedDto() else null,
                beforeApplyingEffect = if (this is BeforeApplyingEffect) toBeforeApplyingEffectDto() else null,
            )

        private object Immediately : Application

        private object OnDefeated : Application

        /** Active from the moment its item is equipped until it is unequipped. */
        private object WhileEquipped : Application

        private data class OnTurnStarted(
            val duration: Int,
        ) : Application {
            constructor(dto: OnTurnStartedDto) : this(duration = dto.duration)

            init {
                if (duration < 0) throw NegativeApplicationDuration()
                if (duration > 99) throw ApplicationDurationAboveLimit()
            }

            fun toTurnStartedDto() = OnTurnStartedDto(duration = duration)
        }

        private data class BeforeApplyingEffect(
            val duration: Int,
        ) : Application {
            constructor(dto: BeforeApplyingEffectDto) : this(duration = dto.duration)

            init {
                if (duration < 0) throw NegativeApplicationDuration()
                if (duration > 99) throw ApplicationDurationAboveLimit()
            }

            fun toBeforeApplyingEffectDto() = BeforeApplyingEffectDto(duration = duration)
        }
    }

    data class Dto(
        val id: String,
        val outcome: EffectOutcomeDto,
        val application: ApplicationDto,
    ) {
        data class EffectApplicationDto(
            val source: ApplicationSourceDto,
            val target: EffectTargetDto,
            val effectId: String,
            val destination: EffectTargetDto?,
        ) {
            data class ApplicationSourceDto(
                val type: SourceTypeDto,
                val battleUnitId: String?,
                val terrainId: String?,
                val lootableItemId: String?,
                // The ability a battle unit cast, when the effect is applied by a cast.
                val abilityId: String?,
            ) {
                enum class SourceTypeDto {
                    BATTLE_UNIT,
                    TERRAIN,
                    ITEM,
                }

                companion object {
                    fun battleUnit(
                        battleUnitId: String,
                        abilityId: String? = null,
                    ) = ApplicationSourceDto(
                        type = SourceTypeDto.BATTLE_UNIT,
                        battleUnitId = battleUnitId,
                        terrainId = null,
                        lootableItemId = null,
                        abilityId = abilityId,
                    )

                    fun terrain(terrainId: String) =
                        ApplicationSourceDto(
                            type = SourceTypeDto.TERRAIN,
                            battleUnitId = null,
                            terrainId = terrainId,
                            lootableItemId = null,
                            abilityId = null,
                        )

                    /** A consumed or equipped item. */
                    fun item(lootableItemId: String) =
                        ApplicationSourceDto(
                            type = SourceTypeDto.ITEM,
                            battleUnitId = null,
                            terrainId = null,
                            lootableItemId = lootableItemId,
                            abilityId = null,
                        )
                }
            }
        }

        /** A battle unit, or a position of the battlefield. */
        data class EffectTargetDto(
            val type: TypeDto,
            val battleUnitId: String?,
            val position: PositionDto?,
        ) {
            companion object {
                fun battleUnit(battleUnitId: String) = EffectTargetDto(type = TypeDto.BATTLE_UNIT, battleUnitId = battleUnitId, position = null)

                fun position(
                    row: Int,
                    column: Int,
                ) = EffectTargetDto(type = TypeDto.POSITION, battleUnitId = null, position = PositionDto(row = row, column = column))
            }

            enum class TypeDto {
                BATTLE_UNIT,
                POSITION,
            }

            data class PositionDto(
                val row: Int,
                val column: Int,
            )
        }

        data class EffectOutcomeDto(
            val type: TypeDto,
            val decreaseHealth: DecreaseHealthDto?,
            val increaseHealth: IncreaseHealthDto?,
            val applyEffectOnNearbyAllies: ApplyEffectOnNearbyAlliesDto?,
            val deployBattleUnit: DeployBattleUnitDto?,
            val increaseMana: IncreaseManaDto?,
            val increaseMaximumHealth: IncreaseMaximumHealthDto?,
            val increaseMaximumMana: IncreaseMaximumManaDto?,
            val learnAbility: LearnAbilityDto?,
        ) {
            companion object {
                fun decreaseHealth(damage: Int) = withoutDetails(TypeDto.DECREASE_HEALTH).copy(decreaseHealth = DecreaseHealthDto(damage = damage))

                fun increaseHealth(healing: Int) = withoutDetails(TypeDto.INCREASE_HEALTH).copy(increaseHealth = IncreaseHealthDto(healing = healing))

                fun negateIncreaseHealth() = withoutDetails(TypeDto.NEGATE_INCREASE_HEALTH)

                fun teleport() = withoutDetails(TypeDto.TELEPORT)

                fun applyEffectOnNearbyAllies(effectId: String) =
                    withoutDetails(TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES).copy(applyEffectOnNearbyAllies = ApplyEffectOnNearbyAlliesDto(effectId = effectId))

                fun deployBattleUnit(unitId: String) = withoutDetails(TypeDto.DEPLOY_BATTLE_UNIT).copy(deployBattleUnit = DeployBattleUnitDto(unitId = unitId))

                fun increaseMana(mana: Int) = withoutDetails(TypeDto.INCREASE_MANA).copy(increaseMana = IncreaseManaDto(mana = mana))

                fun increaseMaximumHealth(healthPoints: Int) = withoutDetails(TypeDto.INCREASE_MAXIMUM_HEALTH).copy(increaseMaximumHealth = IncreaseMaximumHealthDto(healthPoints = healthPoints))

                fun increaseMaximumMana(manaPoints: Int) = withoutDetails(TypeDto.INCREASE_MAXIMUM_MANA).copy(increaseMaximumMana = IncreaseMaximumManaDto(manaPoints = manaPoints))

                fun learnAbility(abilityId: String) = withoutDetails(TypeDto.LEARN_ABILITY).copy(learnAbility = LearnAbilityDto(abilityId = abilityId))

                // The outcome of the type, before the factory sets its own details.
                private fun withoutDetails(type: TypeDto) =
                    EffectOutcomeDto(
                        type = type,
                        decreaseHealth = null,
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = null,
                        increaseMana = null,
                        increaseMaximumHealth = null,
                        increaseMaximumMana = null,
                        learnAbility = null,
                    )
            }

            enum class TypeDto {
                DECREASE_HEALTH,
                INCREASE_HEALTH,
                NEGATE_INCREASE_HEALTH,
                TELEPORT,
                APPLY_EFFECT_ON_NEARBY_ALLIES,
                DEPLOY_BATTLE_UNIT,
                INCREASE_MANA,
                INCREASE_MAXIMUM_HEALTH,
                INCREASE_MAXIMUM_MANA,
                LEARN_ABILITY,
            }

            data class DecreaseHealthDto(
                val damage: Int,
            )

            data class IncreaseHealthDto(
                val healing: Int,
            )

            data class ApplyEffectOnNearbyAlliesDto(
                val effectId: String,
            )

            data class DeployBattleUnitDto(
                val unitId: String,
            )

            data class IncreaseManaDto(
                val mana: Int,
            )

            data class IncreaseMaximumHealthDto(
                val healthPoints: Int,
            )

            data class IncreaseMaximumManaDto(
                val manaPoints: Int,
            )

            data class LearnAbilityDto(
                val abilityId: String,
            )
        }

        data class ApplicationDto(
            val type: ApplicationTypeDto,
            val onTurnStarted: OnTurnStartedDto?,
            val beforeApplyingEffect: BeforeApplyingEffectDto?,
        ) {
            enum class ApplicationTypeDto {
                IMMEDIATELY,
                ON_TURN_STARTED,
                BEFORE_APPLYING_EFFECT,
                ON_DEFEATED,
                WHILE_EQUIPPED,
            }

            data class OnTurnStartedDto(
                val duration: Int,
            )

            data class BeforeApplyingEffectDto(
                val duration: Int,
            )
        }
    }
}
