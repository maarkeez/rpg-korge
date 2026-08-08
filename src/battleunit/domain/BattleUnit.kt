package battleunit.domain

import battleunit.domain.BattleUnitEvent.BattleUnitDeployed
import player.domain.Player
import unit.domain.Unit
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
    private val events: Set<BattleUnitEvent>,
){

    companion object {
        fun deploy(id: String, unit: Unit.Dto, player: Player.Dto, deployAtRow: Int, deployAtColumn: Int): BattleUnit {
            return BattleUnit(
                Id(id),
                UnitId(unit.id),
                PlayerId(player.id),
                RemainingHealthPoints(unit.healthPoints),
                RemainingManaPoints(unit.manaPoints),
                RemainingTurnActions(unit.movementRange),
                AbilityCooldowns(unit.abilities),
                OngoingEffects(),
                events = setOf(BattleUnitDeployed(battleUnitId = id, row = deployAtRow, column = deployAtColumn))
            )
        }
    }

    fun pullEvents() = events to copy(events = emptySet())

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
        constructor(movementRange: Int): this(RemainingSteps(movementRange), RemainingCasts(1))
        @JvmInline private value class RemainingSteps(val value: Int)
        @JvmInline private value class RemainingCasts(val value: Int)
    }
    @JvmInline private value class AbilityCooldowns(val value: Map<AbilityId, CooldownTurnsLeft>){
        constructor(abilities: List<String>) : this(abilities.associate {
            abilityId -> AbilityId(abilityId) to CooldownTurnsLeft(0)
        })

        @JvmInline private value class AbilityId(val value: String)
        @JvmInline private value class CooldownTurnsLeft(val value: Int)
    }
    @JvmInline private value class OngoingEffects(val value: List<Effect>){
        constructor(): this(emptyList())

        private data class Effect(val effectId: EffectId, val applicationStatus: ApplicationStatus)
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
