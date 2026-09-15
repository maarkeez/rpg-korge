package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
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
        fun create(dto: Dto): Effect =
            Effect(
                id = Id(dto.id),
                outcome = Outcome(dto.outcome),
                application = Application(dto.application),
                events = setOf(EffectEvent.EffectCreated(dto.id)),
            )
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
                }
        }

        fun toDto() =
            when (this) {
                is DecreaseHealth ->
                    Dto.EffectOutcomeDto(
                        type = DECREASE_HEALTH,
                        increaseHealth = null,
                        decreaseHealth = this.toDecreaseHealthDto(),
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = null,
                    )
                is IncreaseHealth ->
                    Dto.EffectOutcomeDto(
                        type = INCREASE_HEALTH,
                        increaseHealth = this.toIncreaseHealthDto(),
                        decreaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = null,
                    )
                NegateIncreaseHealth ->
                    Dto.EffectOutcomeDto(
                        type = NEGATE_INCREASE_HEALTH,
                        increaseHealth = null,
                        decreaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = null,
                    )
                Teleport ->
                    Dto.EffectOutcomeDto(
                        type = TELEPORT,
                        increaseHealth = null,
                        decreaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = null,
                    )
                is ApplyEffectOnNearbyAllies ->
                    Dto.EffectOutcomeDto(
                        type = APPLY_EFFECT_ON_NEARBY_ALLIES,
                        increaseHealth = null,
                        decreaseHealth = null,
                        applyEffectOnNearbyAllies = this.toApplyEffectOnNearbyAlliesDto(),
                        deployBattleUnit = null,
                    )
                is DeployBattleUnit ->
                    Dto.EffectOutcomeDto(
                        type = DEPLOY_BATTLE_UNIT,
                        increaseHealth = null,
                        decreaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = this.toDeployBattleUnitDto(),
                    )
            }

        @JvmInline private value class DecreaseHealth(
            val damage: Int,
        ) : Outcome {
            init {
                if (damage < 0) throw NegativePower()
                if (damage > 999) throw PowerAboveLimit()
            }

            fun toDecreaseHealthDto() = Dto.EffectOutcomeDto.DecreaseHealthDto(damage = damage)
        }

        @JvmInline private value class IncreaseHealth(
            val healing: Int,
        ) : Outcome {
            init {
                if (healing < 0) throw NegativePower()
                if (healing > 999) throw PowerAboveLimit()
            }

            fun toIncreaseHealthDto() = Dto.EffectOutcomeDto.IncreaseHealthDto(healing = healing)
        }

        @JvmInline private value class ApplyEffectOnNearbyAllies(
            val effectId: String,
        ) : Outcome {
            fun toApplyEffectOnNearbyAlliesDto() = Dto.EffectOutcomeDto.ApplyEffectOnNearbyAlliesDto(effectId = effectId)
        }

        @JvmInline private value class DeployBattleUnit(
            val unitId: String,
        ) : Outcome {
            fun toDeployBattleUnitDto() = Dto.EffectOutcomeDto.DeployBattleUnitDto(unitId = unitId)
        }

        private object NegateIncreaseHealth : Outcome

        private object Teleport : Outcome
    }

    private sealed interface Application {
        companion object {
            const val IMMEDIATELY = "IMMEDIATELY"
            const val ON_TURN_STARTED = "ON_TURN_STARTED"
            const val BEFORE_APPLYING_EFFECT = "BEFORE_APPLYING_EFFECT"
            const val ON_DEFEATED = "ON_DEFEATED"

            operator fun invoke(dto: ApplicationDto): Application =
                when (dto.type) {
                    IMMEDIATELY -> Immediately
                    ON_TURN_STARTED -> OnTurnStarted(dto.onTurnStarted ?: throw MissingEffectApplicationDetails())
                    BEFORE_APPLYING_EFFECT -> BeforeApplyingEffect(dto.beforeApplyingEffect ?: throw MissingEffectApplicationDetails())
                    ON_DEFEATED -> OnDefeated
                    else -> throw InvalidEffectApplication()
                }
        }

        fun toDto() =
            ApplicationDto(
                type =
                    when (this) {
                        is Immediately -> IMMEDIATELY
                        is OnTurnStarted -> ON_TURN_STARTED
                        is BeforeApplyingEffect -> BEFORE_APPLYING_EFFECT
                        is OnDefeated -> ON_DEFEATED
                    },
                onTurnStarted = if (this is OnTurnStarted) toTurnStartedDto() else null,
                beforeApplyingEffect = if (this is BeforeApplyingEffect) toBeforeApplyingEffectDto() else null,
            )

        private object Immediately : Application

        private object OnDefeated : Application

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
        data class EffectOutcomeDto(
            val type: TypeDto,
            val decreaseHealth: DecreaseHealthDto?,
            val increaseHealth: IncreaseHealthDto?,
            val applyEffectOnNearbyAllies: ApplyEffectOnNearbyAlliesDto?,
            val deployBattleUnit: DeployBattleUnitDto? = null,
        ) {
            enum class TypeDto {
                DECREASE_HEALTH,
                INCREASE_HEALTH,
                NEGATE_INCREASE_HEALTH,
                TELEPORT,
                APPLY_EFFECT_ON_NEARBY_ALLIES,
                DEPLOY_BATTLE_UNIT,
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
        }

        data class ApplicationDto(
            val type: String,
            val onTurnStarted: OnTurnStartedDto?,
            val beforeApplyingEffect: BeforeApplyingEffectDto?,
        ) {
            data class OnTurnStartedDto(
                val duration: Int,
            )

            data class BeforeApplyingEffectDto(
                val duration: Int,
            )
        }
    }
}
