package battleunit.domain

import ability.domain.Ability
import battleunit.domain.BattleUnitError.EffectNotFound
import battleunit.domain.BattleUnitError.MovementDistanceExceedsRemainingSteps
import battleunit.domain.BattleUnitError.MovementDistanceMustBeGreaterThanZero
import battleunit.domain.BattleUnitError.NotDelayedEffect
import battleunit.domain.BattleUnitError.RemainingManaPointsBelowZero
import battleunit.domain.BattleUnitEvent.BattleUnitDamaged
import battleunit.domain.BattleUnitEvent.BattleUnitDefeated
import battleunit.domain.BattleUnitEvent.BattleUnitDeployed
import battleunit.domain.BattleUnitEvent.BattleUnitHealed
import battleunit.domain.BattleUnitEvent.BattleUnitMoved
import battleunit.domain.BattleUnitEvent.BattleUnitTeleported
import battleunit.domain.BattleUnitEvent.EffectReceived
import effect.domain.Effect
import player.domain.Player
import unit.domain.Unit
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

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
        ongoingEffects = ongoingEffects.toDto()
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

    fun teleport(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): BattleUnit {
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

    fun reduceCoolDowns(): BattleUnit {
        val abilityCooldowns = abilityCooldowns.reduceCoolDowns()
        return copy(abilityCooldowns = abilityCooldowns)
    }

    fun canMoveDistance(distance: Int) = remainingTurnActions.canMoveDistance(distance)
    fun isSamePlayer(battleUnit: BattleUnit): Boolean = battleUnit.playerId == playerId
    fun canCastAbility(ability: Ability.Dto): Boolean {
      return remainingTurnActions.canCastAbility()
          && abilityCooldowns.canCastAbility(ability.id)
          && remainingManaPoints.value >= ability.cooldown
    }

    fun castAbility(abilityId: String, abilityCooldown: Int, abilityCost: Int, row: Int, column: Int): BattleUnit {
        val remainingTurnActions = remainingTurnActions.castAbility()
        val abilityCooldowns = abilityCooldowns.castAbility(abilityId, abilityCooldown)
        val abilityCastedEvent = BattleUnitEvent.AbilityCasted(
            battleUnitId = id.value,
            abilityId = abilityId,
            row = row,
            column = column
        )
        return copy(
            remainingManaPoints = RemainingManaPoints(remainingManaPoints.value - abilityCost),
            remainingTurnActions = remainingTurnActions,
            abilityCooldowns = abilityCooldowns,
            events = events + abilityCastedEvent
        )
    }

    fun receiveImmediateEffect(effectId: String): BattleUnit {
        val ongoingEffects = ongoingEffects.receiveImmediateEffect(effectId)
        val effectReceivedEvent = EffectReceived(
            battleUnitId = id.value,
            effectId = effectId
        )
        return copy(
            ongoingEffects = ongoingEffects,
            events = events + effectReceivedEvent
        )
    }

    fun receiveDelayedEffect(effectId: String, turnsLeft: Int): BattleUnit {
        val ongoingEffects = ongoingEffects.receiveDelayedEffect(effectId, turnsLeft)
        val effectReceivedEvent = EffectReceived(
            battleUnitId = id.value,
            effectId = effectId
        )
        return copy(
            ongoingEffects = ongoingEffects,
            events = events + effectReceivedEvent
        )
    }

    fun isDefeated() : Boolean = remainingHealthPoints.value <= 0

    fun applyImmediateEffect(effect: Effect.Dto, unit: Unit.Dto): BattleUnit {
        val ongoingEffects = ongoingEffects.applyPendingEffect(effect.id)
        return when (effect.type) {
            "DECREASE_HEALTH" -> {
                applyDecreaseHealthEffect(effect, ongoingEffects)
            }
            "INCREASE_HEALTH" -> {
                val remainingHealthPoints =
                    RemainingHealthPoints(min(unit.healthPoints, remainingHealthPoints.value + effect.power))
                val healedEvent = BattleUnitHealed(id.value)
                copy(
                    ongoingEffects = ongoingEffects,
                    remainingHealthPoints = remainingHealthPoints,
                    events = events + healedEvent
                )
            }
            "TELEPORT" -> {
                copy(
                    events = events + BattleUnitTeleported(battleUnitId = id.value)
                )
            }
            else -> {
                TODO("Not implemented yet")
            }
        }
    }

    fun hasDelayedOngoingEffects(): Boolean {
        return ongoingEffects.hasDelayedOngoingEffects()
    }

    fun applyDelayedEffect(effect: Effect.Dto): BattleUnit {
        val ongoingEffects = ongoingEffects.applyDelayedEffect(effect.id)
        return if(effect.type == "DECREASE_HEALTH") {
            applyDecreaseHealthEffect(effect, ongoingEffects)
        }else{
            TODO("Not implemented yet")
        }
    }

    private fun applyDecreaseHealthEffect(
        effect: Effect.Dto,
        ongoingEffects: OngoingEffects
    ): BattleUnit {
        val remainingHealthPoints = RemainingHealthPoints(max(0, remainingHealthPoints.value - effect.power))
        val newEvents = buildList {
            add(BattleUnitDamaged(battleUnitId = id.value))
            if (remainingHealthPoints.value <= 0) {
                add(
                    BattleUnitDefeated(
                        playerId = playerId.value,
                        battleUnitId = id.value
                    )
                )
            }
        }
        return copy(
            ongoingEffects = ongoingEffects,
            remainingHealthPoints = remainingHealthPoints,
            events = events + newEvents
        )
    }

    @JvmInline private value class Id(val value: String)
    @JvmInline private value class UnitId(val value: String)
    @JvmInline private value class PlayerId(val value: String)
    @JvmInline private value class RemainingHealthPoints(val value: Int)
    @JvmInline private value class RemainingManaPoints(val value: Int) {
        init {
            if(value < 0) throw RemainingManaPointsBelowZero()
        }
    }
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
        @JvmInline private value class CooldownTurnsLeft(val value: Int){
            fun reduce() = CooldownTurnsLeft(max(0, value - 1))
        }

        fun toDto(): Map<String, Int> = value.entries.associate { it.key.value to it.value.value }

        fun canCastAbility(abilityId: String): Boolean {
            val cooldownTurnsLeft = value[AbilityId(abilityId)] ?: return false
            return cooldownTurnsLeft.value == 0
        }

        fun castAbility(abilityId: String, abilityCooldown: Int): AbilityCooldowns {
            val abilityCooldowns = buildMap {
                value.entries.forEach {
                    if(it.key.value == abilityId){
                        put(AbilityId(abilityId), CooldownTurnsLeft(abilityCooldown))
                    }else {
                        put(it.key, it.value)
                    }
                }
            }
            return AbilityCooldowns(abilityCooldowns)
        }

        fun reduceCoolDowns(): AbilityCooldowns {
            val abilityCooldowns = buildMap {
                value.entries.forEach { (abilityId, cooldownTurnsLeft) ->
                    put(abilityId, cooldownTurnsLeft.reduce())
                }
            }
            return AbilityCooldowns(abilityCooldowns)
        }
    }
    @JvmInline private value class OngoingEffects(val value: List<Effect>){
        fun receiveImmediateEffect(effectId: String): OngoingEffects {
            val newEffect = Effect(EffectId(effectId), ApplicationStatus.pending())
            return OngoingEffects(value + newEffect)
        }

        fun receiveDelayedEffect(effectId: String, turnsLeft: Int): OngoingEffects {
            val newEffect = Effect(EffectId(effectId), ApplicationStatus.delayed(turnsLeft))
            return OngoingEffects(value + newEffect)
        }

        fun applyPendingEffect(effectId: String): OngoingEffects {
            // TODO: Validate type
            return OngoingEffects(value.filter { it.effectId.value != effectId })
        }

        fun applyDelayedEffect(effectId: String): OngoingEffects {
            val effect = value.firstOrNull { it.effectId.value == effectId } ?: throw EffectNotFound()
            if(!effect.applicationStatus.isDelayed()) throw NotDelayedEffect()
            val turnsLeft = (effect.applicationStatus as ApplicationStatus.TurnsLeft).apply()
            val ongoingEffects = if(turnsLeft != null){
                buildList {
                    value.forEach { ongoingEffect ->
                        if(ongoingEffect.effectId.value == effectId){
                            add(ongoingEffect.copy(applicationStatus = turnsLeft))
                        }else{
                            add(ongoingEffect)
                        }
                    }
                }
            }else{
                value.filter { it.effectId.value != effectId }
            }
            return OngoingEffects(ongoingEffects)
        }

        fun hasDelayedOngoingEffects(): Boolean = value.any { it.applicationStatus.isDelayed() }
        fun toDto(): Dto.OngoingEffectsDto {
            return Dto.OngoingEffectsDto(
                delayedEffects = value.filter { it.applicationStatus.isDelayed() }.map { it.effectId.value }
            )
        }

        constructor(): this(emptyList())

        private data class Effect(val effectId: EffectId, val applicationStatus: ApplicationStatus)
        @JvmInline private value class EffectId(val value: String)
        private sealed interface ApplicationStatus {
            object Pending : ApplicationStatus
            @JvmInline value class TurnsLeft(val value: Int) : ApplicationStatus {
                fun apply(): TurnsLeft? = if(value - 1 == 0) null else TurnsLeft(value - 1)
            }

            companion object {
                fun pending(): ApplicationStatus = Pending
                fun delayed(turnsLeft: Int): ApplicationStatus = TurnsLeft(turnsLeft)
            }

            fun isDelayed() = this is TurnsLeft
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
        val ongoingEffects: OngoingEffectsDto,
    ){
        data class RemainingTurnActionsDto(
            val remainingCasts: Int,
            val remainingSteps: Int,
        )

        data class OngoingEffectsDto(
            val delayedEffects: List<String>,
        )
    }
}
