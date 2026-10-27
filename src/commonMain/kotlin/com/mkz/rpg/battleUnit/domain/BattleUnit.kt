package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilitiesAboveLimit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityAlreadyKnown
import com.mkz.rpg.battleUnit.domain.BattleUnitError.EffectNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.EquippedEffectAlreadyApplied
import com.mkz.rpg.battleUnit.domain.BattleUnitError.EquippedEffectsNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.InvalidEffectApplicationStatus
import com.mkz.rpg.battleUnit.domain.BattleUnitError.MovementDistanceExceedsRemainingSteps
import com.mkz.rpg.battleUnit.domain.BattleUnitError.MovementDistanceMustBeGreaterThanZero
import com.mkz.rpg.battleUnit.domain.BattleUnitError.NotOnTurnStarted
import com.mkz.rpg.battleUnit.domain.BattleUnitError.RemainingManaPointsBelowZero
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.AbilityLearned
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDamaged
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitHealed
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitManaRestored
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitRespawned
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitTeleported
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitVanished
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EffectReceived
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.EquippedEffectsRemoved
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.OnTurnStartedEffectApplied
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestMoveOccupant
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTile
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestRemoveOccupant
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MANA
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MAXIMUM_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MAXIMUM_MANA
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.LEARN_ABILITY
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.NEGATE_INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.KnownAbilities
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
    private val baseMaximumHealthPoints: BaseMaximumHealthPoints,
    private val baseMaximumManaPoints: BaseMaximumManaPoints,
    private val equippedEffects: EquippedEffects,
    private val remainingTurnActions: RemainingTurnActions,
    private val abilityCooldowns: AbilityCooldowns,
    private val learnedAbilities: LearnedAbilities,
    private val ongoingEffects: OngoingEffects,
    private val remainingTurns: RemainingTurns?,
    // Its own events, each followed by the battlefield request it needs (e.g.: occupying the tile it was deployed on),
    // published together so the battlefield is up to date before anything reacting to the next events.
    private val events: Set<DomainEvent>,
) {
    companion object {
        fun deploy(
            id: String,
            unit: Unit.Dto,
            player: Player.Dto,
            deployAtRow: Int,
            deployAtColumn: Int,
            remainingTurns: Int? = null,
        ): BattleUnit =
            BattleUnit(
                Id(id),
                UnitId(unit.id),
                PlayerId(player.id),
                RemainingHealthPoints(unit.healthPoints),
                RemainingManaPoints(unit.manaPoints),
                BaseMaximumHealthPoints(unit.healthPoints),
                BaseMaximumManaPoints(unit.manaPoints),
                EquippedEffects(),
                RemainingTurnActions(unit.movementRange),
                AbilityCooldowns(unit.abilities),
                LearnedAbilities(),
                OngoingEffects(),
                remainingTurns?.let(::RemainingTurns),
                events =
                    setOf(
                        BattleUnitDeployed(playerId = player.id, battleUnitId = id, row = deployAtRow, column = deployAtColumn),
                        RequestOccupyTile(occupantId = id, row = deployAtRow, column = deployAtColumn),
                    ),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            playerId = playerId.value,
            remainingHealthPoints = remainingHealthPoints.value,
            remainingManaPoints = remainingManaPoints.value,
            maximumHealthPoints = maximumHealthPoints(),
            maximumManaPoints = maximumManaPoints(),
            equippedEffects = equippedEffects.toDto(),
            remainingTurnActions = remainingTurnActions.toDto(),
            abilityCooldowns = abilityCooldowns.toDto(),
            unitId = unitId.value,
            ongoingEffects = ongoingEffects.toDto(),
            remainingTurns = remainingTurns?.value,
        )

    /**
     * Path: the tiles walked from the origin (excluded) to the destination (included). Each tile costs one step.
     */
    fun move(
        fromRow: Int,
        fromColumn: Int,
        path: List<Battlefield.Dto.PositionDto>,
    ): BattleUnit {
        val remainingTurnActions = remainingTurnActions.move(path.size)
        val destination = path.last()
        val movedEvent =
            BattleUnitMoved(
                battleUnitId = id.value,
                fromRow = fromRow,
                fromColumn = fromColumn,
                toRow = destination.row,
                toColumn = destination.column,
                path = path,
            )
        return copy(
            remainingTurnActions = remainingTurnActions,
            events = events + movedEvent + movedEvent.occupantMoveRequest(),
        )
    }

    /**
     * Walks outside a battle (e.g.: while exploring), so it spends no turn action.
     * Path: the tiles walked from the origin (excluded) to the destination (included).
     */
    fun walk(
        fromRow: Int,
        fromColumn: Int,
        path: List<Battlefield.Dto.PositionDto>,
    ): BattleUnit {
        val destination = path.last()
        val movedEvent =
            BattleUnitMoved(
                battleUnitId = id.value,
                fromRow = fromRow,
                fromColumn = fromColumn,
                toRow = destination.row,
                toColumn = destination.column,
                path = path,
            )
        return copy(events = events + movedEvent + movedEvent.occupantMoveRequest())
    }

    /**
     * Walks outside a battle onto the tile of the battle unit it swaps with, which walks onto its tile at the same time
     * (e.g.: the leader of the party and the party member on its next tile). The battlefield swaps both occupants at
     * once, so it is not requested to move this one.
     */
    fun swap(
        fromRow: Int,
        fromColumn: Int,
        toRow: Int,
        toColumn: Int,
        swappedWithBattleUnitId: String,
    ): BattleUnit {
        val movedEvent =
            BattleUnitMoved(
                battleUnitId = id.value,
                fromRow = fromRow,
                fromColumn = fromColumn,
                toRow = toRow,
                toColumn = toColumn,
                path = listOf(Battlefield.Dto.PositionDto(row = toRow, column = toColumn)),
                swappedWithBattleUnitId = swappedWithBattleUnitId,
            )
        return copy(events = events + movedEvent)
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
                // A teleport jumps straight to the destination.
                path = listOf(Battlefield.Dto.PositionDto(row = toRow, column = toColumn)),
            )
        return copy(
            remainingTurnActions = remainingTurnActions,
            events = events + movedEvent + movedEvent.occupantMoveRequest(),
        )
    }

    private fun BattleUnitMoved.occupantMoveRequest() = RequestMoveOccupant(occupantId = battleUnitId, toRow = toRow, toColumn = toColumn)

    /**
     * A summon counts the turns of its player: once the turn it is on finishes, it has one turn fewer, and it vanishes
     * when none is left. Nothing changes for the other battle units, nor once defeated.
     */
    fun finishTurn(
        row: Int,
        column: Int,
    ): BattleUnit {
        if (remainingTurns == null || isDefeated()) return this
        val turnsLeft = remainingTurns.finishTurn()
        return if (turnsLeft.isOver()) vanish(row = row, column = column) else copy(remainingTurns = turnsLeft)
    }

    /**
     * A summon vanishes from its tile, whatever turns it has left (e.g.: when the battle ends). Nothing changes for the
     * other battle units, nor once defeated.
     */
    fun vanish(
        row: Int,
        column: Int,
    ): BattleUnit {
        if (remainingTurns == null || isDefeated()) return this
        return copy(
            remainingTurns = RemainingTurns(0),
            events =
                events +
                    BattleUnitVanished(
                        playerId = playerId.value,
                        battleUnitId = id.value,
                        unitId = unitId.value,
                        row = row,
                        column = column,
                    ) +
                    RequestRemoveOccupant(occupantId = id.value),
        )
    }

    /**
     * A defeated battle unit comes back on the tile as it was deployed with its unit: full health and mana, no equipped
     * effects, no cooldowns, no ongoing effects. It keeps its id, its player and the abilities it learned, after the
     * abilities of its unit. Nothing changes when it is not defeated.
     */
    fun respawn(
        unit: Unit.Dto,
        row: Int,
        column: Int,
    ): BattleUnit {
        if (!isDefeated()) return this
        return copy(
            unitId = UnitId(unit.id),
            remainingHealthPoints = RemainingHealthPoints(unit.healthPoints),
            remainingManaPoints = RemainingManaPoints(unit.manaPoints),
            baseMaximumHealthPoints = BaseMaximumHealthPoints(unit.healthPoints),
            baseMaximumManaPoints = BaseMaximumManaPoints(unit.manaPoints),
            equippedEffects = EquippedEffects(),
            remainingTurnActions = RemainingTurnActions(unit.movementRange),
            abilityCooldowns = AbilityCooldowns(unit.abilities + learnedAbilities.value),
            ongoingEffects = OngoingEffects(),
            events =
                events +
                    BattleUnitRespawned(playerId = playerId.value, battleUnitId = id.value, row = row, column = column) +
                    RequestOccupyTile(occupantId = id.value, row = row, column = column),
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

    fun replenishMana(): BattleUnit {
        val remainingManaPoints = RemainingManaPoints(min(remainingManaPoints.value + 10, maximumManaPoints()))
        return copy(remainingManaPoints = remainingManaPoints)
    }

    /**
     * Recovers the percentage of its maximum health and mana (e.g.: while wandering out of the battles), rounded up and
     * up to the maximums. A defeated battle unit does not recover, so it stays defeated.
     */
    fun recover(percentage: Int): BattleUnit {
        if (isDefeated()) return this
        val remainingHealthPoints = RemainingHealthPoints(min(remainingHealthPoints.value + shareOf(maximumHealthPoints(), percentage), maximumHealthPoints()))
        val remainingManaPoints = RemainingManaPoints(min(remainingManaPoints.value + shareOf(maximumManaPoints(), percentage), maximumManaPoints()))
        return copy(remainingHealthPoints = remainingHealthPoints, remainingManaPoints = remainingManaPoints)
    }

    // The percentage of the maximum, rounded up, so any maximum gives at least 1.
    private fun shareOf(
        maximum: Int,
        percentage: Int,
    ): Int = (maximum * percentage + 99) / 100

    /** The unit's maximum health, raised by the effects of the equipped items. */
    private fun maximumHealthPoints(): Int = baseMaximumHealthPoints.value + equippedEffects.maximumHealthBonus()

    /** The unit's maximum mana, raised by the effects of the equipped items. */
    private fun maximumManaPoints(): Int = baseMaximumManaPoints.value + equippedEffects.maximumManaBonus()

    /** Raises a maximum stat while the item is equipped. The remaining health and mana do not change. */
    fun applyEquippedEffect(
        lootableItemId: String,
        effect: Effect.Dto,
    ): BattleUnit {
        val equippedEffects = equippedEffects.apply(lootableItemId, effect)
        val effectReceivedEvent = EffectReceived(battleUnitId = id.value, effectId = effect.id)
        return copy(
            equippedEffects = equippedEffects,
            events = events + effectReceivedEvent,
        )
    }

    /** Removes the effects of an unequipped item, lowering the remaining health and mana to the new maximums when they are above them. */
    fun removeEquippedEffects(lootableItemId: String): BattleUnit {
        val unequipped = copy(equippedEffects = equippedEffects.remove(lootableItemId))
        return unequipped.copy(
            remainingHealthPoints = RemainingHealthPoints(min(remainingHealthPoints.value, unequipped.maximumHealthPoints())),
            remainingManaPoints = RemainingManaPoints(min(remainingManaPoints.value, unequipped.maximumManaPoints())),
            events = events + EquippedEffectsRemoved(battleUnitId = id.value, lootableItemId = lootableItemId),
        )
    }

    fun canMoveDistance(distance: Int) = remainingTurnActions.canMoveDistance(distance)

    fun isSamePlayer(battleUnit: BattleUnit): Boolean = battleUnit.playerId == playerId

    /** Whether it can learn the ability for good: it does not know it yet, and knows fewer abilities than the most it can. */
    fun canLearnAbility(abilityId: String): Boolean = !abilityCooldowns.knows(abilityId) && abilityCooldowns.value.size < KnownAbilities.MAXIMUM

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
        val remainingTurnActions = remainingTurnActions.reduceRemainingCasts()
        val abilityCooldowns = abilityCooldowns.setAbilityCooldown(abilityId, abilityCooldown)
        val remainingManaPoints = RemainingManaPoints(remainingManaPoints.value - abilityCost)
        val abilityCastedEvent =
            BattleUnitEvent.AbilityCasted(
                battleUnitId = id.value,
                abilityId = abilityId,
                castGroup = castGroup,
            )
        return copy(
            remainingManaPoints = remainingManaPoints,
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
                    RemainingHealthPoints(min(maximumHealthPoints(), remainingHealthPoints.value + effect.outcome.increaseHealth!!.healing))
                val healedEvent = BattleUnitHealed(id.value)
                copy(
                    ongoingEffects = ongoingEffects,
                    remainingHealthPoints = remainingHealthPoints,
                    events = events + healedEvent,
                )
            }
            INCREASE_MANA -> {
                val remainingManaPoints =
                    RemainingManaPoints(min(maximumManaPoints(), remainingManaPoints.value + effect.outcome.increaseMana!!.mana))
                val manaRestoredEvent = BattleUnitManaRestored(id.value)
                copy(
                    ongoingEffects = ongoingEffects,
                    remainingManaPoints = remainingManaPoints,
                    events = events + manaRestoredEvent,
                )
            }
            TELEPORT -> {
                copy(
                    events = events + BattleUnitTeleported(battleUnitId = id.value),
                )
            }
            LEARN_ABILITY -> copy(ongoingEffects = ongoingEffects).learnAbility(effect.outcome.learnAbility!!.abilityId)
            NEGATE_INCREASE_HEALTH,
            APPLY_EFFECT_ON_NEARBY_ALLIES,
            DEPLOY_BATTLE_UNIT,
            INCREASE_MAXIMUM_HEALTH,
            INCREASE_MAXIMUM_MANA,
            -> TODO("Not implemented")
        }
    }

    /** Learns the ability for good, after the abilities it knows, ready to be cast. */
    private fun learnAbility(abilityId: String): BattleUnit {
        if (abilityCooldowns.knows(abilityId)) throw AbilityAlreadyKnown()
        if (!canLearnAbility(abilityId)) throw AbilitiesAboveLimit()
        return copy(
            abilityCooldowns = abilityCooldowns.learn(abilityId),
            learnedAbilities = learnedAbilities.learn(abilityId),
            events = events + AbilityLearned(battleUnitId = id.value, abilityId = abilityId),
        )
    }

    fun hasOnTurnStartedEffects(): Boolean = ongoingEffects.hasOnTurnStartedEffects()

    fun applyOnTurnStartedEffect(
        effect: Effect.Dto,
        currentRow: Int,
        currentColumn: Int,
    ): BattleUnit {
        val ongoingEffects = ongoingEffects.applyOnTurnStartedEffect(effect.id)
        val appliedEvent = OnTurnStartedEffectApplied(battleUnitId = id.value, effectId = effect.id)
        return if (effect.outcome.type == DECREASE_HEALTH) {
            copy(events = events + appliedEvent).applyDecreaseHealthEffect(effect, ongoingEffects, currentRow, currentColumn)
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
                            unitId = unitId.value,
                            defeatedAtRow = currentRow,
                            defeatedAtColumn = currentColumn,
                        ),
                    )
                    add(RequestRemoveOccupant(occupantId = id.value))
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

    /** The turns a summon has left, counting the one it is on. */
    @JvmInline private value class RemainingTurns(
        val value: Int,
    ) {
        fun finishTurn() = RemainingTurns(max(0, value - 1))

        fun isOver() = value == 0
    }

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

    @JvmInline private value class BaseMaximumHealthPoints(
        val value: Int,
    )

    @JvmInline private value class BaseMaximumManaPoints(
        val value: Int,
    )

    /** The effects of the equipped items, which last until their item is unequipped. */
    @JvmInline private value class EquippedEffects(
        val value: List<EquippedEffect>,
    ) {
        constructor() : this(emptyList())

        fun apply(
            lootableItemId: String,
            effect: Effect.Dto,
        ): EquippedEffects {
            val bonus =
                when (effect.outcome.type) {
                    INCREASE_MAXIMUM_HEALTH -> Bonus.MaximumHealth(effect.outcome.increaseMaximumHealth!!.healthPoints)
                    INCREASE_MAXIMUM_MANA -> Bonus.MaximumMana(effect.outcome.increaseMaximumMana!!.manaPoints)
                    else -> throw InvalidEffectApplicationStatus()
                }
            val equippedEffect = EquippedEffect(LootableItemId(lootableItemId), EffectId(effect.id), bonus)
            if (value.any { it.isSameEffect(equippedEffect) }) throw EquippedEffectAlreadyApplied()
            return EquippedEffects(value + equippedEffect)
        }

        fun remove(lootableItemId: String): EquippedEffects {
            val remainingEffects = value.filterNot { it.lootableItemId.value == lootableItemId }
            if (remainingEffects.size == value.size) throw EquippedEffectsNotFound()
            return EquippedEffects(remainingEffects)
        }

        fun maximumHealthBonus(): Int = value.sumOf { (it.bonus as? Bonus.MaximumHealth)?.healthPoints ?: 0 }

        fun maximumManaBonus(): Int = value.sumOf { (it.bonus as? Bonus.MaximumMana)?.manaPoints ?: 0 }

        fun toDto(): Map<String, List<String>> =
            value
                .groupBy { it.lootableItemId.value }
                .mapValues { (_, effects) -> effects.map { it.effectId.value } }

        private data class EquippedEffect(
            val lootableItemId: LootableItemId,
            val effectId: EffectId,
            val bonus: Bonus,
        ) {
            fun isSameEffect(other: EquippedEffect) = lootableItemId == other.lootableItemId && effectId == other.effectId
        }

        @JvmInline private value class LootableItemId(
            val value: String,
        )

        @JvmInline private value class EffectId(
            val value: String,
        )

        private sealed interface Bonus {
            @JvmInline value class MaximumHealth(
                val healthPoints: Int,
            ) : Bonus

            @JvmInline value class MaximumMana(
                val manaPoints: Int,
            ) : Bonus
        }
    }

    private data class RemainingTurnActions(
        private val movementRange: MovementRange,
        private val remainingSteps: RemainingSteps,
        private val remainingCasts: RemainingCasts,
    ) {
        constructor(movementRange: Int) : this(MovementRange(movementRange), RemainingSteps(movementRange), RemainingCasts(CASTS_PER_TURN))

        private companion object {
            const val CASTS_PER_TURN = 1
        }

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
                maximumSteps = movementRange.value,
                maximumCasts = CASTS_PER_TURN,
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
                remainingCasts = RemainingCasts(CASTS_PER_TURN),
            )

        fun canCastAbility(): Boolean = remainingCasts.value > 0

        fun reduceRemainingCasts() = copy(remainingCasts = RemainingCasts(remainingCasts.value - 1))
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

        fun knows(abilityId: String): Boolean = AbilityId(abilityId) in value

        // The ability is added after the known ones, with no cooldown.
        fun learn(abilityId: String) = AbilityCooldowns(value + (AbilityId(abilityId) to CooldownTurnsLeft(0)))

        fun canCastAbility(abilityId: String): Boolean {
            val cooldownTurnsLeft = value[AbilityId(abilityId)] ?: return false
            return cooldownTurnsLeft.value == 0
        }

        fun setAbilityCooldown(
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

    /** The abilities learned for good (e.g.: by consuming an item), which are kept when it respawns. */
    @JvmInline private value class LearnedAbilities(
        val value: List<String>,
    ) {
        constructor() : this(emptyList())

        fun learn(abilityId: String) = LearnedAbilities(value + abilityId)
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
        // Including the effects of the equipped items.
        val maximumHealthPoints: Int,
        val maximumManaPoints: Int,
        // The effect ids applied by each equipped lootable item id.
        val equippedEffects: Map<String, List<String>>,
        val remainingTurnActions: RemainingTurnActionsDto,
        val abilityCooldowns: Map<String, Int>,
        val unitId: String,
        val ongoingEffects: OngoingEffectsDto,
        // The turns a summon has left, counting the one it is on. None for the battle units that never vanish.
        val remainingTurns: Int?,
    ) {
        data class RemainingTurnActionsDto(
            val remainingCasts: Int,
            val remainingSteps: Int,
            val maximumSteps: Int,
            val maximumCasts: Int,
        )

        data class OngoingEffectsDto(
            val onTurnStarted: List<String>,
            val onDefeatedEffects: List<String>,
        )
    }
}
