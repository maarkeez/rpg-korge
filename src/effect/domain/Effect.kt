package effect.domain

import effect.domain.Effect.Dto.ApplicationDto
import effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import effect.domain.Effect.Dto.ModifierDto
import effect.domain.Effect.Dto.ModifierDto.StackDto
import effect.domain.EffectError.ApplicationDurationAboveLimit
import effect.domain.EffectError.EmptyEffectId
import effect.domain.EffectError.InvalidEffectApplication
import effect.domain.EffectError.InvalidEffectModifier
import effect.domain.EffectError.InvalidEffectType
import effect.domain.EffectError.MissingEffectApplicationDetails
import effect.domain.EffectError.NegativeApplicationDuration
import effect.domain.EffectError.NegativePower
import effect.domain.EffectError.NegativeProbability
import effect.domain.EffectError.PowerAboveLimit
import effect.domain.EffectError.ProbabilityAboveLimit
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Effect private constructor(
    private val id: Id,
    private val type: Type,
    private val power: Power,
    private val probability: Probability,
    private val modifiers: Modifiers,
    private val application: Application,
    private val events: Set<EffectEvent>,
) {

    companion object {
        fun create(dto: Dto): Effect {
            return Effect(
                id = Id(dto.id),
                type = Type(dto.type),
                power = Power(dto.power),
                probability = Probability(dto.probability),
                modifiers = Modifiers(dto.modifiers),
                application = Application(dto.application),
                events = setOf(EffectEvent.EffectCreated(dto.id))
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() = Dto(
        id = id.value,
        type = type.toDto(),
        power = power.value,
        probability = probability.value,
        modifiers = modifiers.value.map(Modifier::toDto),
        application = application.toDto()
    )


    @JvmInline private value class Id(val value: String){
        init {
            if(value.isBlank()) throw EmptyEffectId()
        }
    }
    @JvmInline private value class Power(val value: Int){
        init {
            if(value<0) throw NegativePower()
            if(value>999) throw PowerAboveLimit()
        }
    }
    @JvmInline private value class Probability(val value: Int){
        init {
            if(value<0) throw NegativeProbability()
            if(value>100) throw ProbabilityAboveLimit()
        }
    }
    @JvmInline private value class Modifiers(val value: List<Modifier>){
        companion object{
            operator fun invoke(modifiers: List<ModifierDto>) = Modifiers(modifiers.map { Modifier.create(it) })
        }
    }

    private enum class Type {
        DECREASE_HEALTH,
        INCREASE_HEALTH,
        NEGATE_INCREASE_HEALTH,
        TELEPORT;

        companion object {
            operator fun invoke(type: String): Type = runCatching { Type.valueOf(type) }.getOrElse { throw InvalidEffectType() }
        }
        fun toDto() = name
    }

    private sealed interface Modifier {
        companion object {
            const val STACK = "STACK"
            fun create(dto: ModifierDto): Modifier {
                return when (dto.type) {
                    STACK -> Stack(dto.stack!!)
                    else -> throw InvalidEffectModifier()
                }
            }
        }
        fun toDto() = ModifierDto(
            type = when(this){
                is Stack -> STACK
            },
            stack = if(this is Stack) toStackDto() else null
        )

        private data class Stack (val maximum: Int) : Modifier {

            constructor(dto: StackDto) :this(maximum = dto.maximum)

            fun toStackDto() = StackDto(maximum = maximum)
        }
    }

    private sealed interface Application {

        companion object {
            const val IMMEDIATELY = "IMMEDIATELY"
            const val ON_TURN_STARTED = "ON_TURN_STARTED"
            const val BEFORE_APPLYING_EFFECT = "BEFORE_APPLYING_EFFECT"
            operator fun invoke(dto: ApplicationDto): Application {
                return when (dto.type) {
                    IMMEDIATELY -> Immediately
                    ON_TURN_STARTED -> OnTurnStarted(dto.onTurnStarted ?: throw MissingEffectApplicationDetails())
                    BEFORE_APPLYING_EFFECT -> BeforeApplyingEffect(dto.beforeApplyingEffect ?: throw MissingEffectApplicationDetails())
                    else -> throw InvalidEffectApplication()
                }
            }
        }

        fun toDto() = ApplicationDto(
            type = when(this){
                is Immediately -> IMMEDIATELY
                is OnTurnStarted -> ON_TURN_STARTED
                is BeforeApplyingEffect -> BEFORE_APPLYING_EFFECT
            },
            onTurnStarted = if(this is OnTurnStarted) toTurnStartedDto() else null,
            beforeApplyingEffect = if(this is BeforeApplyingEffect) toBeforeApplyingEffectDto() else null
        )

        private object Immediately : Application

        private data class OnTurnStarted(val duration: Int): Application{
            constructor(dto: OnTurnStartedDto) :this(duration = dto.duration)
            init {
                if(duration<0) throw NegativeApplicationDuration()
                if(duration>99) throw ApplicationDurationAboveLimit()
            }
            fun toTurnStartedDto() = OnTurnStartedDto(duration = duration)

        }
        private data class BeforeApplyingEffect(val duration: Int): Application{
            constructor(dto: BeforeApplyingEffectDto) :this(duration = dto.duration)
            init {
                if(duration<0) throw NegativeApplicationDuration()
                if(duration>99) throw ApplicationDurationAboveLimit()
            }
            fun toBeforeApplyingEffectDto() = BeforeApplyingEffectDto(duration = duration)
        }
    }

    data class Dto(
        val id: String,
        val type: String,
        val power: Int,
        val probability: Int,
        val modifiers: List<ModifierDto>,
        val application: ApplicationDto,
    ){
        data class ModifierDto(
            val type: String,
            val stack: StackDto?,
        ){
            data class StackDto(
                val maximum: Int,
            )
        }
        data class ApplicationDto(
            val type: String,
            val onTurnStarted: OnTurnStartedDto?,
            val beforeApplyingEffect: BeforeApplyingEffectDto?,
        ){
            data class OnTurnStartedDto(val duration: Int)
            data class BeforeApplyingEffectDto(val duration: Int)
        }
    }
}
