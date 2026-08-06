package battleunit.domain

import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class BattleUnit private constructor(
    private val id: Id,
    private val unitId: UnitId,
    private val playerId: PlayerId,
    private val remainingHealthPoints: RemainingHealthPoints,
    private val remainingManaPoints: RemainingManaPoints,
    private val remainingTurnActions: RemainingTurnActions,
    private val abilityCooldowns: AbilityCooldowns,
    private val ongoingEffects: OngoingEffects,
){

    fun toDto() = Dto(
        id = id.value,
    )
    @JvmInline private value class Id(val value: String)
    @JvmInline private value class UnitId(val value: String)
    @JvmInline private value class PlayerId(val value: String)
    @JvmInline private value class RemainingHealthPoints(val value: Int)
    @JvmInline private value class RemainingManaPoints(val value: Int)
    private data class RemainingTurnActions(
        private val remainingSteps: RemainingSteps,
        private val remainingCasts: RemainingCasts
    ){
        @JvmInline private value class RemainingSteps(val value: Int)
        @JvmInline private value class RemainingCasts(val value: Int)
    }
    @JvmInline private value class AbilityCooldowns(val value: Map<AbilityId, CooldownTurnsLeft>){
        @JvmInline private value class AbilityId(val value: String)
        @JvmInline private value class CooldownTurnsLeft(val value: Int)
    }
    @JvmInline private value class OngoingEffects(val value: String){
        @JvmInline private value class EffectId(val value: String)
        private sealed interface ApplicationStatus {
            private object Pending : ApplicationStatus
            @JvmInline private value class TurnsLeft(val value: Int) : ApplicationStatus
        }
    }

    data class Dto(
        val id: String,
    )
}
