package battleunit.domain

import battleunit.domain.BattleUnitError.MovementDistanceExceedsRemainingSteps
import battleunit.domain.BattleUnitError.MovementDistanceMustBeGreaterThanZero
import battleunit.domain.BattleUnitEvent.BattleUnitDeployed
import battleunit.domain.BattleUnitEvent.BattleUnitMoved
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
        playerId = playerId.value,
        remainingHealthPoints = remainingHealthPoints.value,
        remainingManaPoints = remainingManaPoints.value,
        remainingTurnActions = remainingTurnActions.toDto(),
        abilityCooldowns = abilityCooldowns.toDto(),
        unitId = unitId.value,
    )

    fun move(
        distance: Int,
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): BattleUnit {
        val remainingTurnActions = remainingTurnActions.move(distance)
        val movedEvent = BattleUnitMoved(
            battleUnitId = id.value,
            fromRow = fromRow,
            fromColumn = fromColumn,
            toRow = toRow,
            toColumn = toColumn
        )
        return copy(
            remainingTurnActions = remainingTurnActions,
            events = events + movedEvent
        )
    }

    fun resetActions(): BattleUnit {
        val remainingTurnActions = remainingTurnActions.reset()
        return copy(remainingTurnActions = remainingTurnActions)
    }

    fun canMoveDistance(distance: Int) = remainingTurnActions.canMoveDistance(distance)
    fun isSamePlayer(battleUnit: BattleUnit): Boolean = battleUnit.playerId == playerId
    fun canCastAbility(abilityId: String): Boolean {
      return remainingTurnActions.canCastAbility() && abilityCooldowns.canCastAbility(abilityId)
    }

    fun castAbility(abilityId: String, abilityCooldown: Int, row: Int, column: Int): BattleUnit {
        val remainingTurnActions = remainingTurnActions.castAbility()
        val abilityCooldowns = abilityCooldowns.castAbility(abilityId, abilityCooldown)
        val abilityCastedEvent = BattleUnitEvent.AbilityCasted(
            battleUnitId = id.value,
            abilityId = abilityId,
            row = row,
            column = column
        )
        return copy(
            remainingTurnActions = remainingTurnActions,
            abilityCooldowns = abilityCooldowns,
            events = events + abilityCastedEvent
        )
    }

    @JvmInline private value class Id(val value: String)
    @JvmInline private value class UnitId(val value: String)
    @JvmInline private value class PlayerId(val value: String)
    @JvmInline private value class RemainingHealthPoints(val value: Int)
    @JvmInline private value class RemainingManaPoints(val value: Int)
    private data class RemainingTurnActions(
        private val movementRange: MovementRange,
        private val remainingSteps: RemainingSteps,
        private val remainingCasts: RemainingCasts
    ){
        constructor(movementRange: Int): this(MovementRange(movementRange), RemainingSteps(movementRange), RemainingCasts(1))
        @JvmInline private value class MovementRange(val value: Int)
        @JvmInline private value class RemainingSteps(val value: Int)
        @JvmInline private value class RemainingCasts(val value: Int)

        fun toDto() = Dto.RemainingTurnActionsDto(
            remainingSteps = remainingSteps.value,
            remainingCasts = remainingCasts.value,
        )

        fun canMoveDistance(distance: Int): Boolean = distance <= remainingSteps.value
        fun move(distance: Int): RemainingTurnActions {
            if(distance <= 0) throw MovementDistanceMustBeGreaterThanZero()
            if(!canMoveDistance(distance)) throw MovementDistanceExceedsRemainingSteps()
            return copy(remainingSteps = RemainingSteps(remainingSteps.value - distance))
        }

        fun reset(): RemainingTurnActions {
            return copy(
                remainingSteps = RemainingSteps(movementRange.value),
                remainingCasts = RemainingCasts(1)
            )
        }

        fun canCastAbility(): Boolean = remainingCasts.value > 0
        fun castAbility() = copy(remainingCasts = RemainingCasts(remainingCasts.value - 1))
    }
    @JvmInline private value class AbilityCooldowns(val value: Map<AbilityId, CooldownTurnsLeft>){

        constructor(abilities: List<String>) : this(abilities.associate {
            abilityId -> AbilityId(abilityId) to CooldownTurnsLeft(0)
        })

        @JvmInline private value class AbilityId(val value: String)
        @JvmInline private value class CooldownTurnsLeft(val value: Int)

        fun toDto(): Map<String, Int> = value.entries.associate { it.key.value to it.value.value }

        fun canCastAbility(abilityId: String): Boolean {
            val cooldownTurnsLeft = value[AbilityId(abilityId)] ?: return false
            return cooldownTurnsLeft.value == 0
        }

        fun castAbility(abilityId: String, abilityCooldown: Int): AbilityCooldowns {
            val abilityCooldowns = buildMap {
                put(AbilityId(abilityId), CooldownTurnsLeft(abilityCooldown))
                value.entries.filter { it.key.value != abilityId }.forEach { put(it.key, it.value) }
            }
            return AbilityCooldowns(abilityCooldowns)
        }
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
        val playerId: String,
        val remainingHealthPoints: Int,
        val remainingManaPoints: Int,
        val remainingTurnActions: RemainingTurnActionsDto,
        val abilityCooldowns: Map<String, Int>,
        val unitId: String,
    ){
        data class RemainingTurnActionsDto(
            val remainingCasts: Int,
            val remainingSteps: Int,
        )
    }
}
