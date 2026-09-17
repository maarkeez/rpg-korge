package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.battleUnit.domain.BattleUnitError.EffectNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.InvalidEffectApplicationStatus
import com.mkz.rpg.battleUnit.domain.BattleUnitError.MovementDistanceExceedsRemainingSteps
import com.mkz.rpg.battleUnit.domain.BattleUnitError.MovementDistanceMustBeGreaterThanZero
import com.mkz.rpg.battleUnit.domain.BattleUnitError.NotOnTurnStarted
import com.mkz.rpg.battleUnit.domain.BattleUnitError.RemainingManaPointsBelowZero
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDamaged
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitHealed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitTeleported
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EffectReceived
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.NEGATE_INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.unit.domain.Unit
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
) {
    companion object {
        fun deploy(
            id: String,
            unit: Unit.Dto,
            player: Player.Dto,
            deployAtRow: Int,
            deployAtColumn: Int,
        ): BattleUnit =
            BattleUnit(
                Id(id),
                UnitId(unit.id),
                PlayerId(player.id),
                RemainingHealthPoints(unit.healthPoints),
                RemainingManaPoints(unit.manaPoints),
                RemainingTurnActions(unit.movementRange),
                AbilityCooldowns(unit.abilities),
                OngoingEffects(),
                events = setOf(BattleUnitDeployed(battleUnitId = id, row = deployAtRow, column = deployAtColumn)),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            playerId = playerId.value,
            remainingHealthPoints = remainingHealthPoints.value,
            remainingManaPoints = remainingManaPoints.value,
            remainingTurnActions = remainingTurnActions.toDto(),
            abilityCooldowns = abilityCooldowns.toDto(),
            unitId = unitId.value,
            ongoingEffects = ongoingEffects.toDto(),
        )

    fun move(
        distance: Int,
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): BattleUnit {
        val remainingTurnActions = remainingTurnActions.move(distance)
        val movedEvent =
            BattleUnitMoved(
                battleUnitId = id.value,
                fromRow = fromRow,
                fromColumn = fromColumn,
                toRow = toRow,
                toColumn = toColumn,
            )
        return copy(
            remainingTurnActions = remainingTurnActions,
            events = events + movedEvent,
        )
    }

    fun teleport(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
    ): BattleUnit {
        val movedEvent =
            BattleUnitMoved(
                battleUnitId = id.value,
                fromRow = fromRow,
                fromColumn = fromColumn,
                toRow = toRow,
                toColumn = toColumn,
            )
        return copy(
            remainingTurnActions = remainingTurnActions,
            events = events + movedEvent,
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

    fun replenishMana(unit: Unit.Dto): BattleUnit {
        val remainingManaPoints = RemainingManaPoints(min(remainingManaPoints.value + 10, unit.manaPoints))
        return copy(remainingManaPoints = remainingManaPoints)
    }

    fun canMoveDistance(distance: Int) = remainingTurnActions.canMoveDistance(distance)

    fun isSamePlayer(battleUnit: BattleUnit): Boolean = battleUnit.playerId == playerId

    fun canCastAbility(ability: Ability.Dto): Boolean =
        remainingTurnActions.canCastAbility() &&
            abilityCooldowns.canCastAbility(ability.id) &&
            remainingManaPoints.value >= ability.cost

    fun castAbility(
        abilityId: String,
        abilityCooldown: Int,
        abilityCost: Int,
        castGroup: List<Battlefield.Dto.PositionDto>,
    ): BattleUnit {
        val remainingTurnActions = remainingTurnActions.castAbility()
        val abilityCooldowns = abilityCooldowns.castAbility(abilityId, abilityCooldown)
        val abilityCastedEvent =
            BattleUnitEvent.AbilityCasted(
                battleUnitId = id.value,
                abilityId = abilityId,
                castGroup = castGroup,
            )
        return copy(
            remainingManaPoints = RemainingManaPoints(remainingManaPoints.value - abilityCost),
            remainingTurnActions = remainingTurnActions,
            abilityCooldowns = abilityCooldowns,
            events = events + abilityCastedEvent,
        )
    }

    fun receiveImmediateEffect(effectId: String): BattleUnit {
        val ongoingEffects = ongoingEffects.receiveImmediateEffect(effectId)
        val effectReceivedEvent =
            EffectReceived(
                battleUnitId = id.value,
                effectId = effectId,
            )
        return copy(
            ongoingEffects = ongoingEffects,
            events = events + effectReceivedEvent,
        )
    }

    fun receiveOnTurnStartedEffect(
        effectId: String,
        turnsLeft: Int,
    ): BattleUnit {
        val ongoingEffects = ongoingEffects.receiveOnTurnStartedEffect(effectId, turnsLeft)
        val effectReceivedEvent =
            EffectReceived(
                battleUnitId = id.value,
                effectId = effectId,
            )
        return copy(
            ongoingEffects = ongoingEffects,
            events = events + effectReceivedEvent,
        )
    }

    fun receiveOnDefeatedEffect(effectId: String): BattleUnit {
        val ongoingEffects = ongoingEffects.receiveOnDefeatedEffect(effectId)
        val effectReceivedEvent =
            EffectReceived(
                battleUnitId = id.value,
                effectId = effectId,
            )
        return copy(
            ongoingEffects = ongoingEffects,
            events = events + effectReceivedEvent,
        )
    }

    fun isDefeated(): Boolean = remainingHealthPoints.value <= 0

    fun applyOnDefeatedEffects(): BattleUnit {
        val ongoingEffects = ongoingEffects.clearOnDefeatedEffects()
        return copy(ongoingEffects = ongoingEffects)
    }

    fun applyImmediateEffect(
        effect: Effect.Dto,
        unit: Unit.Dto,
        currentRow: Int,
        currentColumn: Int,
    ): BattleUnit {
        val ongoingEffects = ongoingEffects.applyImmediateEffect(effect.id)
        return when (effect.outcome.type) {
            DECREASE_HEALTH -> {
                applyDecreaseHealthEffect(effect, ongoingEffects, currentRow, currentColumn)
            }
            INCREASE_HEALTH -> {
                val remainingHealthPoints =
                    RemainingHealthPoints(min(unit.healthPoints, remainingHealthPoints.value + effect.outcome.increaseHealth!!.healing))
                val healedEvent = BattleUnitHealed(id.value)
                copy(
                    ongoingEffects = ongoingEffects,
                    remainingHealthPoints = remainingHealthPoints,
                    events = events + healedEvent,
                )
            }
            TELEPORT -> {
                copy(
                    events = events + BattleUnitTeleported(battleUnitId = id.value),
                )
            }
            NEGATE_INCREASE_HEALTH,
            APPLY_EFFECT_ON_NEARBY_ALLIES,
            DEPLOY_BATTLE_UNIT,
            -> TODO("Not implemented")
        }
    }

    fun hasOnTurnStartedEffects(): Boolean = ongoingEffects.hasOnTurnStartedEffects()

    fun applyOnTurnStartedEffect(
        effect: Effect.Dto,
        currentRow: Int,
        currentColumn: Int,
    ): BattleUnit {
        val ongoingEffects = ongoingEffects.applyOnTurnStartedEffect(effect.id)
        return if (effect.outcome.type == DECREASE_HEALTH) {
            applyDecreaseHealthEffect(effect, ongoingEffects, currentRow, currentColumn)
        } else {
            TODO("Not implemented yet")
        }
    }

    private fun applyDecreaseHealthEffect(
        effect: Effect.Dto,
        ongoingEffects: OngoingEffects,
        currentRow: Int,
        currentColumn: Int,
    ): BattleUnit {
        val remainingHealthPoints = RemainingHealthPoints(max(0, remainingHealthPoints.value - effect.outcome.decreaseHealth!!.damage))
        val newEvents =
            buildList {
                add(BattleUnitDamaged(battleUnitId = id.value))
                if (remainingHealthPoints.value <= 0) {
                    add(
                        BattleUnitDefeated(
                            playerId = playerId.value,
                            battleUnitId = id.value,
                            defeatedAtRow = currentRow,
                            defeatedAtColumn = currentColumn,
                        ),
                    )
                }
            }
        return copy(
            ongoingEffects = ongoingEffects,
            remainingHealthPoints = remainingHealthPoints,
            events = events + newEvents,
        )
    }

    @JvmInline private value class Id(
        val value: String,
    )

    @JvmInline private value class UnitId(
        val value: String,
    )

    @JvmInline private value class PlayerId(
        val value: String,
    )

    @JvmInline private value class RemainingHealthPoints(
        val value: Int,
    )

    @JvmInline private value class RemainingManaPoints(
        val value: Int,
    ) {
        init {
            if (value < 0) throw RemainingManaPointsBelowZero()
        }
    }

    private data class RemainingTurnActions(
        private val movementRange: MovementRange,
        private val remainingSteps: RemainingSteps,
        private val remainingCasts: RemainingCasts,
    ) {
        constructor(movementRange: Int) : this(MovementRange(movementRange), RemainingSteps(movementRange), RemainingCasts(1))

        @JvmInline private value class MovementRange(
            val value: Int,
        )

        @JvmInline private value class RemainingSteps(
            val value: Int,
        )

        @JvmInline private value class RemainingCasts(
            val value: Int,
        )

        fun toDto() =
            Dto.RemainingTurnActionsDto(
                remainingSteps = remainingSteps.value,
                remainingCasts = remainingCasts.value,
            )

        fun canMoveDistance(distance: Int): Boolean = distance <= remainingSteps.value

        fun move(distance: Int): RemainingTurnActions {
            if (distance <= 0) throw MovementDistanceMustBeGreaterThanZero()
            if (!canMoveDistance(distance)) throw MovementDistanceExceedsRemainingSteps()
            return copy(remainingSteps = RemainingSteps(remainingSteps.value - distance))
        }

        fun reset(): RemainingTurnActions =
            copy(
                remainingSteps = RemainingSteps(movementRange.value),
                remainingCasts = RemainingCasts(1),
            )

        fun canCastAbility(): Boolean = remainingCasts.value > 0

        fun castAbility() = copy(remainingCasts = RemainingCasts(remainingCasts.value - 1))
    }

    @JvmInline private value class AbilityCooldowns(
        val value: Map<AbilityId, CooldownTurnsLeft>,
    ) {
        constructor(abilities: List<String>) : this(
            abilities.associate { abilityId ->
                AbilityId(abilityId) to CooldownTurnsLeft(0)
            },
        )

        @JvmInline private value class AbilityId(
            val value: String,
        )

        @JvmInline private value class CooldownTurnsLeft(
            val value: Int,
        ) {
            fun reduce() = CooldownTurnsLeft(max(0, value - 1))
        }

        fun toDto(): Map<String, Int> = value.entries.associate { it.key.value to it.value.value }

        fun canCastAbility(abilityId: String): Boolean {
            val cooldownTurnsLeft = value[AbilityId(abilityId)] ?: return false
            return cooldownTurnsLeft.value == 0
        }

        fun castAbility(
            abilityId: String,
            abilityCooldown: Int,
        ): AbilityCooldowns {
            val abilityCooldowns =
                buildMap {
                    value.entries.forEach {
                        if (it.key.value == abilityId) {
                            put(AbilityId(abilityId), CooldownTurnsLeft(abilityCooldown))
                        } else {
                            put(it.key, it.value)
                        }
                    }
                }
            return AbilityCooldowns(abilityCooldowns)
        }

        fun reduceCoolDowns(): AbilityCooldowns {
            val abilityCooldowns =
                buildMap {
                    value.entries.forEach { (abilityId, cooldownTurnsLeft) ->
                        put(abilityId, cooldownTurnsLeft.reduce())
                    }
                }
            return AbilityCooldowns(abilityCooldowns)
        }
    }

    @JvmInline private value class OngoingEffects(
        val value: List<Effect>,
    ) {
        fun receiveImmediateEffect(effectId: String): OngoingEffects {
            val newEffect = Effect.immediate(effectId)
            return OngoingEffects(value + newEffect)
        }

        fun receiveOnTurnStartedEffect(
            effectId: String,
            turnsLeft: Int,
        ): OngoingEffects {
            val newEffect = Effect.onTurnStarted(effectId, turnsLeft)
            return OngoingEffects(value + newEffect)
        }

        fun receiveOnDefeatedEffect(effectId: String): OngoingEffects {
            val newEffect = Effect.onDefeated(effectId)
            return OngoingEffects(value + newEffect)
        }

        fun applyImmediateEffect(effectId: String): OngoingEffects = OngoingEffects(value.filterNot { it.isImmediate() && it.hasEffectId(effectId) })

        fun applyOnTurnStartedEffect(effectId: String): OngoingEffects {
            val effect = value.firstOrNull { it.hasEffectId(effectId) } ?: throw EffectNotFound()
            if (!effect.isOnTurnStarted()) throw NotOnTurnStarted()
            val effectAfterApplication = effect.applyOnTurnStarted()
            val ongoingEffects =
                if (effectAfterApplication != null) {
                    buildList {
                        value.forEach { ongoingEffect ->
                            if (ongoingEffect.hasEffectId(effectId)) {
                                add(effectAfterApplication)
                            } else {
                                add(ongoingEffect)
                            }
                        }
                    }
                } else {
                    value.filterNot { it.hasEffectId(effectId) }
                }
            return OngoingEffects(ongoingEffects)
        }

        fun hasOnTurnStartedEffects(): Boolean = value.any { it.isOnTurnStarted() }

        fun clearOnDefeatedEffects(): OngoingEffects = OngoingEffects(value.filter { !it.isOnDefeated() })

        fun toDto(): Dto.OngoingEffectsDto =
            Dto.OngoingEffectsDto(
                onTurnStarted = value.filter { it.isOnTurnStarted() }.map { it.effectId() },
                onDefeatedEffects = value.filter { it.isOnDefeated() }.map { it.effectId() },
            )

        constructor() : this(emptyList())

        @ConsistentCopyVisibility
        private data class Effect private constructor(
            private val effectId: EffectId,
            private val applicationStatus: ApplicationStatus,
        ) {
            companion object {
                fun immediate(effectId: String) = Effect(EffectId(effectId), ApplicationStatus.immediate())

                fun onTurnStarted(
                    effectId: String,
                    turnsLeft: Int,
                ) = Effect(EffectId(effectId), ApplicationStatus.onTurnStarted(turnsLeft))

                fun onDefeated(effectId: String) = Effect(EffectId(effectId), ApplicationStatus.onDefeated())
            }

            fun isOnTurnStarted() = applicationStatus.isOnTurnStarted()

            fun isOnDefeated() = applicationStatus.isOnDefeated()

            fun isImmediate() = applicationStatus.isImmediate()

            fun applyOnTurnStarted(): Effect? {
                if (!isOnTurnStarted()) throw InvalidEffectApplicationStatus()
                val updatedApplicationStatus = (applicationStatus as ApplicationStatus.OnTurnStarted).apply()
                return updatedApplicationStatus?.let { copy(applicationStatus = updatedApplicationStatus) }
            }

            fun hasEffectId(effectId: String) = effectId == this.effectId.value

            fun effectId() = effectId.value
        }

        @JvmInline private value class EffectId(
            val value: String,
        )

        private sealed interface ApplicationStatus {
            object Immediate : ApplicationStatus

            object OnDefeated : ApplicationStatus

            @JvmInline value class OnTurnStarted(
                val turnsLeft: Int,
            ) : ApplicationStatus {
                fun apply(): OnTurnStarted? = if (turnsLeft - 1 == 0) null else OnTurnStarted(turnsLeft - 1)
            }

            companion object {
                fun immediate(): ApplicationStatus = Immediate

                fun onDefeated(): ApplicationStatus = OnDefeated

                fun onTurnStarted(turnsLeft: Int): ApplicationStatus = OnTurnStarted(turnsLeft)
            }

            fun isOnTurnStarted() = this is OnTurnStarted

            fun isOnDefeated() = this is OnDefeated

            fun isImmediate() = this is Immediate
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
    ) {
        data class RemainingTurnActionsDto(
            val remainingCasts: Int,
            val remainingSteps: Int,
        )

        data class OngoingEffectsDto(
            val onTurnStarted: List<String>,
            val onDefeatedEffects: List<String>,
        )
    }
}
