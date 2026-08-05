package effect.domain

import effect.domain.EffectError.InvalidEffectModifier
import effect.domain.EffectError.InvalidEffectType
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Effect private constructor(
    private val id: Id,
    private val type: Type,
    private val power: Power,
    private val probability: Probability,
    private val modifiers: Modifiers,
    private val application: Application,
) {

    companion object {
        fun create(dto: Dto): Effect {
            return Effect(
                id = Id(dto.id),
                type = Type(dto.type),
                power = Power(dto.power),
                probability = Probability(dto.probability),
                modifiers = Modifiers(dto.modifiers)
                application = TODO()
            )
        }
    }
    
    @JvmInline value class Id(val value: String)
    @JvmInline value class Power(val value: Int)
    @JvmInline value class Probability(val value: Int)
    @JvmInline value class Modifiers(val value: List<Modifier>){
        companion object{
            operator fun invoke(modifiers: List<Modifier.Dto>) = Modifiers(modifiers.map(::Modifier))
        }
    }

    enum class Type {
        DECREASE_HEALTH,
        INCREASE_HEALTH,
        NEGATE_INCREASE_HEALTH,
        TELEPORT;

        companion object {
            operator fun invoke(type: String): Type = runCatching { Type.valueOf(type) }.getOrElse { throw InvalidEffectType() }
        }
        fun toDto() = name
    }

    sealed interface Modifier {
        companion object {
            operator fun invoke(dto: Dto) {
                TODO("Implement")
            }
        }
        data class Stack(val maximum: Int) : Modifier {

            fun toStackDto() = Dto(maximum = maximum)

            data class Dto(
                val maximum: Int,
            )
        }

        fun toDto() = Dto(
            type = when(this){
                is Stack -> "STACK"
            },
            stack = if(this is Stack) toStackDto() else null
        )
        data class Dto(
            val type: String,
            val stack: Stack.Dto?,
        )
    }

    sealed interface Application {
        object Immediately : Application
        data class OnTurnStarted(val duration: Int): Application{
            fun toTurnStartedDto() = Dto(duration = duration)
            data class Dto(val duration: Int)
        }
        data class BeforeApplyingEffect(val duration: Int): Application{
            fun toBeforeApplyingEffectDto() = Dto(duration = duration)
            data class Dto(val duration: Int)
        }

        fun toDto() = Dto(
            type = when(this){
                is Immediately -> "IMMEDIATELY"
                is OnTurnStarted -> "ON_TURN_STARTED"
                is BeforeApplyingEffect -> "BEFORE_APPLYING_EFFECT"
            },
            onTurnStarted = if(this is OnTurnStarted) toTurnStartedDto() else null,
            beforeApplyingEffect = if(this is BeforeApplyingEffect) toBeforeApplyingEffectDto() else null
        )
        data class Dto(
            val type: String,
            val onTurnStarted: OnTurnStarted.Dto?,
            val beforeApplyingEffect: BeforeApplyingEffect.Dto?,
        )
    }

    fun toDto() = Dto(
        id = id.value,
        type = type.toDto(),
        power = power.value,
        probability = probability.value,
        modifiers = modifiers.value.map(Modifier::toDto),
        application = application.toDto()
    )

    data class Dto(
        val id: String,
        val type: String,
        val power: Int,
        val probability: Int,
        val modifiers: List<Modifier.Dto>,
        val application: Application.Dto,
    )
}
