package effect.domain

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
    
    @JvmInline value class Id(val value: String)
    @JvmInline value class Power(val value: Int)
    @JvmInline value class Probability(val value: Int)
    @JvmInline value class Modifiers(val value: List<Modifier>)

    enum class Type {
        DECREASE_HEALTH,
        INCREASE_HEALTH,
        NEGATE_INCREASE_HEALTH,
        TELEPORT,
    }

    sealed interface Modifier {
        data class Stack(val maximum: Int) : Modifier
    }

    sealed interface Application {
        object Immediately : Application
        data class OnTurnStarted(val duration: Int): Application
        data class BeforeApplyingEffect(val duration: Int): Application
    }
}
