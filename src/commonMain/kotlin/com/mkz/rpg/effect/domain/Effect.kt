package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.TypeDto.NEGATE_INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.TypeDto.TELEPORT
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
    private val type: Type,
    private val application: Application,
    private val events: Set<EffectEvent>,
) {
    companion object {
        fun create(dto: Dto): Effect =
            Effect(
                id = Id(dto.id),
                type = Type(dto.type),
                application = Application(dto.application),
                events = setOf(EffectEvent.EffectCreated(dto.id)),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            type = type.toDto(),
            application = application.toDto(),
        )

    @JvmInline private value class Id(
        val value: String,
    ) {
        init {
            if (value.isBlank()) throw EmptyEffectId()
        }
    }

    private sealed interface Type {
        companion object {
            operator fun invoke(dto: Dto.EffectTypeDto): Type =
                when (dto.type) {
                    DECREASE_HEALTH -> DecreaseHealth(dto.decreaseHealth!!.damage)
                    INCREASE_HEALTH -> IncreaseHealth(dto.increaseHealth!!.healing)
                    NEGATE_INCREASE_HEALTH -> NegateIncreaseHealth
                    TELEPORT -> Teleport
                }
        }

        fun toDto() =
            when (this) {
                is DecreaseHealth ->
                    Dto.EffectTypeDto(
                        type = DECREASE_HEALTH,
                        increaseHealth = null,
                        decreaseHealth = this.toDecreaseHealthDto(),
                    )
                is IncreaseHealth ->
                    Dto.EffectTypeDto(
                        type = INCREASE_HEALTH,
                        increaseHealth = this.toIncreaseHealthDto(),
                        decreaseHealth = null,
                    )
                NegateIncreaseHealth ->
                    Dto.EffectTypeDto(
                        type = NEGATE_INCREASE_HEALTH,
                        increaseHealth = null,
                        decreaseHealth = null,
                    )
                Teleport ->
                    Dto.EffectTypeDto(
                        type = TELEPORT,
                        increaseHealth = null,
                        decreaseHealth = null,
                    )
            }

        @JvmInline private value class DecreaseHealth(
            val damage: Int,
        ) : Type {
            init {
                if (damage < 0) throw NegativePower()
                if (damage > 999) throw PowerAboveLimit()
            }

            fun toDecreaseHealthDto() = Dto.EffectTypeDto.DecreaseHealthDto(damage = damage)
        }

        @JvmInline private value class IncreaseHealth(
            val healing: Int,
        ) : Type {
            init {
                if (healing < 0) throw NegativePower()
                if (healing > 999) throw PowerAboveLimit()
            }

            fun toIncreaseHealthDto() = Dto.EffectTypeDto.IncreaseHealthDto(healing = healing)
        }

        private object NegateIncreaseHealth : Type

        private object Teleport : Type
    }

    private sealed interface Application {
        companion object {
            const val IMMEDIATELY = "IMMEDIATELY"
            const val ON_TURN_STARTED = "ON_TURN_STARTED"
            const val BEFORE_APPLYING_EFFECT = "BEFORE_APPLYING_EFFECT"

            operator fun invoke(dto: ApplicationDto): Application =
                when (dto.type) {
                    IMMEDIATELY -> Immediately
                    ON_TURN_STARTED -> OnTurnStarted(dto.onTurnStarted ?: throw MissingEffectApplicationDetails())
                    BEFORE_APPLYING_EFFECT -> BeforeApplyingEffect(dto.beforeApplyingEffect ?: throw MissingEffectApplicationDetails())
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
                    },
                onTurnStarted = if (this is OnTurnStarted) toTurnStartedDto() else null,
                beforeApplyingEffect = if (this is BeforeApplyingEffect) toBeforeApplyingEffectDto() else null,
            )

        private object Immediately : Application

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
        val type: EffectTypeDto,
        val application: ApplicationDto,
    ) {
        data class EffectTypeDto(
            val type: TypeDto,
            val decreaseHealth: DecreaseHealthDto?,
            val increaseHealth: IncreaseHealthDto?,
        ) {
            enum class TypeDto {
                DECREASE_HEALTH,
                INCREASE_HEALTH,
                NEGATE_INCREASE_HEALTH,
                TELEPORT,
            }

            data class DecreaseHealthDto(
                val damage: Int,
            )

            data class IncreaseHealthDto(
                val healing: Int,
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
