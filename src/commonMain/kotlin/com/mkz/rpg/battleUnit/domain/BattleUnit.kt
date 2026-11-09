package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilitiesAboveLimit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityAlreadyKnown
import com.mkz.rpg.battleUnit.domain.BattleUnitError.EffectNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.EquippedEffectAlreadyApplied
import com.mkz.rpg.battleUnit.domain.BattleUnitError.EquippedEffectsNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitError.InvalidEffectTiming
import com.mkz.rpg.battleUnit.domain.BattleUnitError.NotOnPlayerTurnStarted
import com.mkz.rpg.battleUnit.domain.BattleUnitError.RemainingManaBelowZero
import com.mkz.rpg.battleUnit.domain.BattleUnitError.WalkingDistanceExceedsRemainingSteps
import com.mkz.rpg.battleUnit.domain.BattleUnitError.WalkingDistanceMustBeGreaterThanZero
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
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.OnPlayerTurnStartedEffectTriggered
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestMoveOccupant
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestOccupyTile
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.RequestRemoveOccupant
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_TO_ADJACENT_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DAMAGE
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.HEAL
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MAXIMUM_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_MAXIMUM_MANA
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.LEARN_ABILITY
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.NEGATE_INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.RESTORE_MANA
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.SUMMON
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.unit.domain.Unit
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

@ConsistentCopyVisibility
data class BattleUnit private constructor(
    private val id: Id,
    private val unitId: UnitId,
    private val playerId: PlayerId,
    private val remainingHealth: RemainingHealth,
    private val remainingMana: RemainingMana,
    private val baseMaximumHealth: BaseMaximumHealth,
    private val baseMaximumMana: BaseMaximumMana,
    private val equippedEffects: EquippedEffects,
    private val remainingActions: RemainingActions,
    private val knownAbilities: KnownAbilities,
    private val learnedAbilities: LearnedAbilities,
    private val maximumAbilities: MaximumAbilities,
    private val ongoingEffects: OngoingEffects,
    private val remainingSummonDuration: RemainingSummonDuration?,
    // Its own events, each followed by the battlefield request it needs (e.g.: occupying the tile it was deployed on),
    // published together so the battlefield is up to date before anything reacting to the next events.
    private val events: Set<DomainEvent>,
) {
    companion object {
        fun deploy(
            id: String,
            unit: Unit.Dto,
            player: Player.Dto,
            maximumAbilities: Int,
            deployAtRow: Int,
            deployAtColumn: Int,
            remainingSummonDuration: Int? = null,
        ): BattleUnit =
            BattleUnit(
                Id(id),
                UnitId(unit.id),
                PlayerId(player.id),
                RemainingHealth(unit.baseMaximumHealth),
                RemainingMana(unit.baseMaximumMana),
                BaseMaximumHealth(unit.baseMaximumHealth),
                BaseMaximumMana(unit.baseMaximumMana),
                EquippedEffects(),
                RemainingActions(unit.maximumSteps),
                KnownAbilities(unit.startingAbilities),
                LearnedAbilities(),
                MaximumAbilities(maximumAbilities),
                OngoingEffects(),
                remainingSummonDuration?.let(::RemainingSummonDuration),
                events =
                    setOf(
                        BattleUnitDeployed(playerId = player.id, battleUnitId = id, row = deployAtRow, column = deployAtColumn),
                        RequestOccupyTile(occupantId = id, row = deployAtRow, column = deployAtColumn),
                    ),
            )

        /** Rebuilds the battle unit as it was when the game was saved, from its [toSnapshot], publishing no events. */
        fun load(snapshot: Dto.SnapshotDto): BattleUnit =
            BattleUnit(
                Id(snapshot.id),
                UnitId(snapshot.unitId),
                PlayerId(snapshot.playerId),
                RemainingHealth(snapshot.remainingHealth),
                RemainingMana(snapshot.remainingMana),
                BaseMaximumHealth(snapshot.baseMaximumHealth),
                BaseMaximumMana(snapshot.baseMaximumMana),
                EquippedEffects.load(snapshot.equippedEffects),
                RemainingActions(snapshot.remainingActions.maximumSteps).load(
                    remainingSteps = snapshot.remainingActions.remainingSteps,
                    remainingCasts = snapshot.remainingActions.remainingCasts,
                ),
                KnownAbilities.load(snapshot.knownAbilities),
                LearnedAbilities(snapshot.learnedAbilityIds),
                MaximumAbilities(snapshot.maximumAbilities),
                OngoingEffects.load(snapshot.ongoingEffects),
                snapshot.remainingSummonDuration?.let(::RemainingSummonDuration),
                events = emptySet(),
            )
    }

    fun pullEvents() = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            id = id.value,
            playerId = playerId.value,
            remainingHealth = remainingHealth.value,
            remainingMana = remainingMana.value,
            maximumHealth = maximumHealth(),
            maximumMana = maximumMana(),
            equippedEffects = equippedEffects.toDto(),
            remainingActions = remainingActions.toDto(),
            knownAbilities = knownAbilities.toDto(),
            unitId = unitId.value,
            ongoingEffects = ongoingEffects.toDto(),
            remainingSummonDuration = remainingSummonDuration?.value,
        )

    /** Its whole state, to save the game: [load] rebuilds it as it is. */
    fun toSnapshot() =
        Dto.SnapshotDto(
            id = id.value,
            unitId = unitId.value,
            playerId = playerId.value,
            remainingHealth = remainingHealth.value,
            remainingMana = remainingMana.value,
            baseMaximumHealth = baseMaximumHealth.value,
            baseMaximumMana = baseMaximumMana.value,
            equippedEffects = equippedEffects.toSnapshot(),
            remainingActions = remainingActions.toDto(),
            knownAbilities = knownAbilities.toDto(),
            learnedAbilityIds = learnedAbilities.value,
            maximumAbilities = maximumAbilities.value,
            ongoingEffects = ongoingEffects.toSnapshot(),
            remainingSummonDuration = remainingSummonDuration?.value,
        )

    /**
     * Path: the tiles walked from the origin (excluded) to the destination (included). Each tile costs one step.
     */
    fun walkInBattle(
        fromRow: Int,
        fromColumn: Int,
        path: List<Battlefield.Dto.PositionDto>,
    ): BattleUnit {
        val remainingActions = remainingActions.walk(path.size)
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
            remainingActions = remainingActions,
            events = events + movedEvent + movedEvent.occupantMoveRequest(),
        )
    }

    /**
     * Walks outside a battle (e.g.: while exploring), so it spends no step.
     * Path: the tiles walked from the origin (excluded) to the destination (included).
     */
    fun walkOutOfBattle(
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
            remainingActions = remainingActions,
            events = events + movedEvent + movedEvent.occupantMoveRequest(),
        )
    }

    private fun BattleUnitMoved.occupantMoveRequest() = RequestMoveOccupant(occupantId = battleUnitId, toRow = toRow, toColumn = toColumn)

    /**
     * A summon counts the player turns of its player: once the one it is on finishes, it has one fewer, and it vanishes
     * when none is left. Nothing changes for the other battle units, nor once defeated.
     */
    fun reduceRemainingSummonDuration(
        row: Int,
        column: Int,
    ): BattleUnit {
        if (remainingSummonDuration == null || isDefeated()) return this
        val reduced = remainingSummonDuration.reduce()
        return if (reduced.isOver()) vanish(row = row, column = column) else copy(remainingSummonDuration = reduced)
    }

    /**
     * A summon vanishes from its tile, whatever summon duration it has left (e.g.: when the battle ends). Nothing changes for the
     * other battle units, nor once defeated.
     */
    fun vanish(
        row: Int,
        column: Int,
    ): BattleUnit {
        if (remainingSummonDuration == null || isDefeated()) return this
        return copy(
            remainingSummonDuration = RemainingSummonDuration(0),
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
     * effects, no cooldowns, no ongoing effects. It keeps its id, its player, its maximum abilities and the abilities it
     * learned, after the abilities of its unit. Nothing changes when it is not defeated.
     */
    fun respawn(
        unit: Unit.Dto,
        row: Int,
        column: Int,
    ): BattleUnit {
        if (!isDefeated()) return this
        return copy(
            unitId = UnitId(unit.id),
            remainingHealth = RemainingHealth(unit.baseMaximumHealth),
            remainingMana = RemainingMana(unit.baseMaximumMana),
            baseMaximumHealth = BaseMaximumHealth(unit.baseMaximumHealth),
            baseMaximumMana = BaseMaximumMana(unit.baseMaximumMana),
            equippedEffects = EquippedEffects(),
            remainingActions = RemainingActions(unit.maximumSteps),
            knownAbilities = KnownAbilities(unit.startingAbilities + learnedAbilities.value),
            ongoingEffects = OngoingEffects(),
            events =
                events +
                    BattleUnitRespawned(playerId = playerId.value, battleUnitId = id.value, row = row, column = column) +
                    RequestOccupyTile(occupantId = id.value, row = row, column = column),
        )
    }

    fun resetActions(): BattleUnit {
        val remainingActions = remainingActions.reset()
        return copy(remainingActions = remainingActions)
    }

    fun reduceCooldowns(): BattleUnit {
        val knownAbilities = knownAbilities.reduceCooldowns()
        return copy(knownAbilities = knownAbilities)
    }

    fun replenishMana(): BattleUnit {
        val remainingMana = RemainingMana(min(remainingMana.value + 10, maximumMana()))
        return copy(remainingMana = remainingMana)
    }

    /**
     * Recovers the percentage of its maximum health and mana (e.g.: while wandering out of the battles), rounded up and
     * up to the maximums. A defeated battle unit does not recover, so it stays defeated.
     */
    fun recover(percentage: Int): BattleUnit {
        if (isDefeated()) return this
        val remainingHealth = RemainingHealth(min(remainingHealth.value + shareOf(maximumHealth(), percentage), maximumHealth()))
        val remainingMana = RemainingMana(min(remainingMana.value + shareOf(maximumMana(), percentage), maximumMana()))
        return copy(remainingHealth = remainingHealth, remainingMana = remainingMana)
    }

    // The percentage of the maximum, rounded up, so any maximum gives at least 1.
    private fun shareOf(
        maximum: Int,
        percentage: Int,
    ): Int = (maximum * percentage + 99) / 100

    /** The unit's maximum health, raised by the effects of the equipped items. */
    private fun maximumHealth(): Int = baseMaximumHealth.value + equippedEffects.maximumHealthBonus()

    /** The unit's maximum mana, raised by the effects of the equipped items. */
    private fun maximumMana(): Int = baseMaximumMana.value + equippedEffects.maximumManaBonus()

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
            remainingHealth = RemainingHealth(min(remainingHealth.value, unequipped.maximumHealth())),
            remainingMana = RemainingMana(min(remainingMana.value, unequipped.maximumMana())),
            events = events + EquippedEffectsRemoved(battleUnitId = id.value, lootableItemId = lootableItemId),
        )
    }

    fun canWalkDistance(distance: Int) = remainingActions.canWalkDistance(distance)

    fun isSamePlayer(battleUnit: BattleUnit): Boolean = battleUnit.playerId == playerId

    /** Whether it can learn the ability for good: it does not know it yet, and knows fewer abilities than the most it can. */
    fun canLearnAbility(abilityId: String): Boolean = !knownAbilities.knows(abilityId) && knownAbilities.value.size < maximumAbilities.value

    fun canCastAbility(ability: Ability.Dto): Boolean =
        remainingActions.canCastAbility() &&
            knownAbilities.canCastAbility(ability.id) &&
            remainingMana.value >= ability.manaCost

    fun castAbility(
        abilityId: String,
        abilityCooldown: Int,
        abilityManaCost: Int,
        castGroup: List<Battlefield.Dto.PositionDto>,
    ): BattleUnit {
        val remainingActions = remainingActions.reduceRemainingCasts()
        val knownAbilities = knownAbilities.setAbilityCooldown(abilityId, abilityCooldown)
        val remainingMana = RemainingMana(remainingMana.value - abilityManaCost)
        val abilityCastedEvent =
            BattleUnitEvent.AbilityCasted(
                battleUnitId = id.value,
                abilityId = abilityId,
                castGroup = castGroup,
            )
        return copy(
            remainingMana = remainingMana,
            remainingActions = remainingActions,
            knownAbilities = knownAbilities,
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

    fun receiveOnPlayerTurnStartedEffect(
        effectId: String,
        remainingDuration: Int,
    ): BattleUnit {
        val ongoingEffects = ongoingEffects.receiveOnPlayerTurnStartedEffect(effectId, remainingDuration)
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

    fun isDefeated(): Boolean = remainingHealth.value <= 0

    fun triggerOnDefeatedEffects(): BattleUnit {
        val ongoingEffects = ongoingEffects.clearOnDefeatedEffects()
        return copy(ongoingEffects = ongoingEffects)
    }

    fun triggerImmediateEffect(
        effect: Effect.Dto,
        currentRow: Int,
        currentColumn: Int,
    ): BattleUnit {
        val ongoingEffects = ongoingEffects.triggerImmediateEffect(effect.id)
        return when (effect.outcome.type) {
            DAMAGE -> {
                applyDecreaseHealthEffect(effect, ongoingEffects, currentRow, currentColumn)
            }
            HEAL -> {
                val remainingHealth =
                    RemainingHealth(min(maximumHealth(), remainingHealth.value + effect.outcome.heal!!.healing))
                val healedEvent = BattleUnitHealed(id.value)
                copy(
                    ongoingEffects = ongoingEffects,
                    remainingHealth = remainingHealth,
                    events = events + healedEvent,
                )
            }
            RESTORE_MANA -> {
                val remainingMana =
                    RemainingMana(min(maximumMana(), remainingMana.value + effect.outcome.restoreMana!!.mana))
                val manaRestoredEvent = BattleUnitManaRestored(id.value)
                copy(
                    ongoingEffects = ongoingEffects,
                    remainingMana = remainingMana,
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
            APPLY_EFFECT_TO_ADJACENT_ALLIES,
            SUMMON,
            INCREASE_MAXIMUM_HEALTH,
            INCREASE_MAXIMUM_MANA,
            -> TODO("Not implemented")
        }
    }

    /** Learns the ability for good, after the abilities it knows, ready to be cast. */
    private fun learnAbility(abilityId: String): BattleUnit {
        if (knownAbilities.knows(abilityId)) throw AbilityAlreadyKnown()
        if (!canLearnAbility(abilityId)) throw AbilitiesAboveLimit()
        return copy(
            knownAbilities = knownAbilities.learn(abilityId),
            learnedAbilities = learnedAbilities.learn(abilityId),
            events = events + AbilityLearned(battleUnitId = id.value, abilityId = abilityId),
        )
    }

    fun hasOnPlayerTurnStartedEffects(): Boolean = ongoingEffects.hasOnPlayerTurnStartedEffects()

    fun triggerOnPlayerTurnStartedEffect(
        effect: Effect.Dto,
        currentRow: Int,
        currentColumn: Int,
    ): BattleUnit {
        val ongoingEffects = ongoingEffects.triggerOnPlayerTurnStartedEffect(effect.id)
        val appliedEvent = OnPlayerTurnStartedEffectTriggered(battleUnitId = id.value, effectId = effect.id)
        return if (effect.outcome.type == DAMAGE) {
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
        val remainingHealth = RemainingHealth(max(0, remainingHealth.value - effect.outcome.damage!!.damage))
        val newEvents =
            buildList {
                add(BattleUnitDamaged(battleUnitId = id.value))
                if (remainingHealth.value <= 0) {
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
            remainingHealth = remainingHealth,
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

    /** The player turns a summon has left, counting the one it is on. */
    @JvmInline private value class RemainingSummonDuration(
        val value: Int,
    ) {
        fun reduce() = RemainingSummonDuration(max(0, value - 1))

        fun isOver() = value == 0
    }

    @JvmInline private value class RemainingHealth(
        val value: Int,
    )

    @JvmInline private value class RemainingMana(
        val value: Int,
    ) {
        init {
            if (value < 0) throw RemainingManaBelowZero()
        }
    }

    @JvmInline private value class BaseMaximumHealth(
        val value: Int,
    )

    @JvmInline private value class BaseMaximumMana(
        val value: Int,
    )

    // The most abilities it knows, those of its unit and those it learned, given by the unit when deployed.
    @JvmInline private value class MaximumAbilities(
        val value: Int,
    )

    /** The effects of the equipped items, which last until their item is unequipped. */
    @JvmInline private value class EquippedEffects(
        val value: List<EquippedEffect>,
    ) {
        constructor() : this(emptyList())

        companion object {
            fun load(snapshots: List<Dto.EquippedEffectSnapshotDto>) =
                EquippedEffects(
                    snapshots.map { snapshot ->
                        val bonus =
                            when (snapshot.bonus) {
                                Dto.EquippedEffectSnapshotDto.BonusDto.MAXIMUM_HEALTH -> Bonus.MaximumHealth(snapshot.amount)
                                Dto.EquippedEffectSnapshotDto.BonusDto.MAXIMUM_MANA -> Bonus.MaximumMana(snapshot.amount)
                            }
                        EquippedEffect(LootableItemId(snapshot.lootableItemId), EffectId(snapshot.effectId), bonus)
                    },
                )
        }

        fun toSnapshot(): List<Dto.EquippedEffectSnapshotDto> =
            value.map { equippedEffect ->
                val (bonus, amount) =
                    when (val equippedBonus = equippedEffect.bonus) {
                        is Bonus.MaximumHealth -> Dto.EquippedEffectSnapshotDto.BonusDto.MAXIMUM_HEALTH to equippedBonus.amount
                        is Bonus.MaximumMana -> Dto.EquippedEffectSnapshotDto.BonusDto.MAXIMUM_MANA to equippedBonus.amount
                    }
                Dto.EquippedEffectSnapshotDto(
                    lootableItemId = equippedEffect.lootableItemId.value,
                    effectId = equippedEffect.effectId.value,
                    bonus = bonus,
                    amount = amount,
                )
            }

        fun apply(
            lootableItemId: String,
            effect: Effect.Dto,
        ): EquippedEffects {
            val bonus =
                when (effect.outcome.type) {
                    INCREASE_MAXIMUM_HEALTH -> Bonus.MaximumHealth(effect.outcome.increaseMaximumHealth!!.maximumHealth)
                    INCREASE_MAXIMUM_MANA -> Bonus.MaximumMana(effect.outcome.increaseMaximumMana!!.maximumMana)
                    else -> throw InvalidEffectTiming()
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

        fun maximumHealthBonus(): Int = value.sumOf { (it.bonus as? Bonus.MaximumHealth)?.amount ?: 0 }

        fun maximumManaBonus(): Int = value.sumOf { (it.bonus as? Bonus.MaximumMana)?.amount ?: 0 }

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
                val amount: Int,
            ) : Bonus

            @JvmInline value class MaximumMana(
                val amount: Int,
            ) : Bonus
        }
    }

    private data class RemainingActions(
        private val maximumSteps: MaximumSteps,
        private val remainingSteps: RemainingSteps,
        private val remainingCasts: RemainingCasts,
    ) {
        constructor(maximumSteps: Int) : this(MaximumSteps(maximumSteps), RemainingSteps(maximumSteps), RemainingCasts(CASTS_PER_PLAYER_TURN))

        private companion object {
            const val CASTS_PER_PLAYER_TURN = 1
        }

        @JvmInline private value class MaximumSteps(
            val value: Int,
        )

        @JvmInline private value class RemainingSteps(
            val value: Int,
        )

        @JvmInline private value class RemainingCasts(
            val value: Int,
        )

        fun toDto() =
            Dto.RemainingActionsDto(
                remainingSteps = remainingSteps.value,
                remainingCasts = remainingCasts.value,
                maximumSteps = maximumSteps.value,
                maximumCasts = CASTS_PER_PLAYER_TURN,
            )

        fun canWalkDistance(distance: Int): Boolean = distance <= remainingSteps.value

        fun walk(distance: Int): RemainingActions {
            if (distance <= 0) throw WalkingDistanceMustBeGreaterThanZero()
            if (!canWalkDistance(distance)) throw WalkingDistanceExceedsRemainingSteps()
            return copy(remainingSteps = RemainingSteps(remainingSteps.value - distance))
        }

        fun reset(): RemainingActions =
            copy(
                remainingSteps = RemainingSteps(maximumSteps.value),
                remainingCasts = RemainingCasts(CASTS_PER_PLAYER_TURN),
            )

        fun canCastAbility(): Boolean = remainingCasts.value > 0

        fun reduceRemainingCasts() = copy(remainingCasts = RemainingCasts(remainingCasts.value - 1))

        fun load(
            remainingSteps: Int,
            remainingCasts: Int,
        ) = copy(remainingSteps = RemainingSteps(remainingSteps), remainingCasts = RemainingCasts(remainingCasts))
    }

    @JvmInline private value class KnownAbilities(
        val value: Map<AbilityId, RemainingCooldown>,
    ) {
        constructor(abilities: List<String>) : this(
            abilities.associate { abilityId ->
                AbilityId(abilityId) to RemainingCooldown(0)
            },
        )

        companion object {
            fun load(remainingCooldowns: Map<String, Int>) =
                KnownAbilities(
                    remainingCooldowns.entries.associate { (abilityId, remainingCooldown) ->
                        AbilityId(abilityId) to
                            RemainingCooldown(remainingCooldown)
                    },
                )
        }

        @JvmInline private value class AbilityId(
            val value: String,
        )

        @JvmInline private value class RemainingCooldown(
            val value: Int,
        ) {
            fun reduce() = RemainingCooldown(max(0, value - 1))
        }

        fun toDto(): Map<String, Int> = value.entries.associate { it.key.value to it.value.value }

        fun knows(abilityId: String): Boolean = AbilityId(abilityId) in value

        // The ability is added after the known ones, with no cooldown.
        fun learn(abilityId: String) = KnownAbilities(value + (AbilityId(abilityId) to RemainingCooldown(0)))

        fun canCastAbility(abilityId: String): Boolean {
            val remainingCooldown = value[AbilityId(abilityId)] ?: return false
            return remainingCooldown.value == 0
        }

        fun setAbilityCooldown(
            abilityId: String,
            abilityCooldown: Int,
        ): KnownAbilities {
            val knownAbilities =
                buildMap {
                    value.entries.forEach {
                        if (it.key.value == abilityId) {
                            put(AbilityId(abilityId), RemainingCooldown(abilityCooldown))
                        } else {
                            put(it.key, it.value)
                        }
                    }
                }
            return KnownAbilities(knownAbilities)
        }

        fun reduceCooldowns(): KnownAbilities {
            val knownAbilities =
                buildMap {
                    value.entries.forEach { (abilityId, remainingCooldown) ->
                        put(abilityId, remainingCooldown.reduce())
                    }
                }
            return KnownAbilities(knownAbilities)
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

        fun receiveOnPlayerTurnStartedEffect(
            effectId: String,
            remainingDuration: Int,
        ): OngoingEffects {
            val newEffect = Effect.onPlayerTurnStarted(effectId, remainingDuration)
            return OngoingEffects(value + newEffect)
        }

        fun receiveOnDefeatedEffect(effectId: String): OngoingEffects {
            val newEffect = Effect.onDefeated(effectId)
            return OngoingEffects(value + newEffect)
        }

        fun triggerImmediateEffect(effectId: String): OngoingEffects = OngoingEffects(value.filterNot { it.isImmediate() && it.hasEffectId(effectId) })

        fun triggerOnPlayerTurnStartedEffect(effectId: String): OngoingEffects {
            val effect = value.firstOrNull { it.hasEffectId(effectId) } ?: throw EffectNotFound()
            if (!effect.isOnPlayerTurnStarted()) throw NotOnPlayerTurnStarted()
            val effectAfterTrigger = effect.triggerOnPlayerTurnStarted()
            val ongoingEffects =
                if (effectAfterTrigger != null) {
                    buildList {
                        value.forEach { ongoingEffect ->
                            if (ongoingEffect.hasEffectId(effectId)) {
                                add(effectAfterTrigger)
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

        fun hasOnPlayerTurnStartedEffects(): Boolean = value.any { it.isOnPlayerTurnStarted() }

        fun clearOnDefeatedEffects(): OngoingEffects = OngoingEffects(value.filter { !it.isOnDefeated() })

        fun toDto(): Dto.OngoingEffectsDto =
            Dto.OngoingEffectsDto(
                onPlayerTurnStarted = value.filter { it.isOnPlayerTurnStarted() }.map { it.effectId() },
                onDefeatedEffects = value.filter { it.isOnDefeated() }.map { it.effectId() },
            )

        constructor() : this(emptyList())

        companion object {
            fun load(snapshots: List<Dto.OngoingEffectSnapshotDto>) =
                OngoingEffects(
                    snapshots.map { snapshot ->
                        when (snapshot.timing) {
                            Dto.OngoingEffectSnapshotDto.TimingDto.IMMEDIATE -> Effect.immediate(snapshot.effectId)
                            Dto.OngoingEffectSnapshotDto.TimingDto.ON_DEFEATED -> Effect.onDefeated(snapshot.effectId)
                            Dto.OngoingEffectSnapshotDto.TimingDto.ON_PLAYER_TURN_STARTED -> Effect.onPlayerTurnStarted(snapshot.effectId, requireNotNull(snapshot.remainingDuration))
                        }
                    },
                )
        }

        fun toSnapshot(): List<Dto.OngoingEffectSnapshotDto> = value.map { effect -> effect.toSnapshot() }

        @ConsistentCopyVisibility
        private data class Effect private constructor(
            private val effectId: EffectId,
            private val timing: Timing,
        ) {
            companion object {
                fun immediate(effectId: String) = Effect(EffectId(effectId), Timing.immediate())

                fun onPlayerTurnStarted(
                    effectId: String,
                    remainingDuration: Int,
                ) = Effect(EffectId(effectId), Timing.onPlayerTurnStarted(remainingDuration))

                fun onDefeated(effectId: String) = Effect(EffectId(effectId), Timing.onDefeated())
            }

            fun isOnPlayerTurnStarted() = timing.isOnPlayerTurnStarted()

            fun isOnDefeated() = timing.isOnDefeated()

            fun isImmediate() = timing.isImmediate()

            fun triggerOnPlayerTurnStarted(): Effect? {
                if (!isOnPlayerTurnStarted()) throw InvalidEffectTiming()
                val updatedTiming = (timing as Timing.OnPlayerTurnStarted).trigger()
                return updatedTiming?.let { copy(timing = updatedTiming) }
            }

            fun hasEffectId(effectId: String) = effectId == this.effectId.value

            fun effectId() = effectId.value

            fun toSnapshot() =
                when (timing) {
                    Timing.Immediate -> Dto.OngoingEffectSnapshotDto(effectId.value, Dto.OngoingEffectSnapshotDto.TimingDto.IMMEDIATE, remainingDuration = null)
                    Timing.OnDefeated -> Dto.OngoingEffectSnapshotDto(effectId.value, Dto.OngoingEffectSnapshotDto.TimingDto.ON_DEFEATED, remainingDuration = null)
                    is Timing.OnPlayerTurnStarted ->
                        Dto.OngoingEffectSnapshotDto(effectId.value, Dto.OngoingEffectSnapshotDto.TimingDto.ON_PLAYER_TURN_STARTED, remainingDuration = timing.remainingDuration)
                }
        }

        @JvmInline private value class EffectId(
            val value: String,
        )

        private sealed interface Timing {
            object Immediate : Timing

            object OnDefeated : Timing

            @JvmInline value class OnPlayerTurnStarted(
                val remainingDuration: Int,
            ) : Timing {
                fun trigger(): OnPlayerTurnStarted? = if (remainingDuration - 1 == 0) null else OnPlayerTurnStarted(remainingDuration - 1)
            }

            companion object {
                fun immediate(): Timing = Immediate

                fun onDefeated(): Timing = OnDefeated

                fun onPlayerTurnStarted(remainingDuration: Int): Timing = OnPlayerTurnStarted(remainingDuration)
            }

            fun isOnPlayerTurnStarted() = this is OnPlayerTurnStarted

            fun isOnDefeated() = this is OnDefeated

            fun isImmediate() = this is Immediate
        }
    }

    data class Dto(
        val id: String,
        val playerId: String,
        val remainingHealth: Int,
        val remainingMana: Int,
        // Including the effects of the equipped items.
        val maximumHealth: Int,
        val maximumMana: Int,
        // The effect ids applied by each equipped lootable item id.
        val equippedEffects: Map<String, List<String>>,
        val remainingActions: RemainingActionsDto,
        val knownAbilities: Map<String, Int>,
        val unitId: String,
        val ongoingEffects: OngoingEffectsDto,
        // The player turns a summon has left, counting the one it is on. None for the battle units that never vanish.
        val remainingSummonDuration: Int?,
    ) {
        data class RemainingActionsDto(
            val remainingCasts: Int,
            val remainingSteps: Int,
            val maximumSteps: Int,
            val maximumCasts: Int,
        )

        data class OngoingEffectsDto(
            val onPlayerTurnStarted: List<String>,
            val onDefeatedEffects: List<String>,
        )

        /**
         * The whole state of a battle unit, to save the game. Base maximums: without the bonuses of the equipped items.
         * Learned abilities: the ones learned for good (e.g.: by consuming an item), also listed in the known abilities, up to its
         * maximum abilities.
         */
        data class SnapshotDto(
            val id: String,
            val unitId: String,
            val playerId: String,
            val remainingHealth: Int,
            val remainingMana: Int,
            val baseMaximumHealth: Int,
            val baseMaximumMana: Int,
            val equippedEffects: List<EquippedEffectSnapshotDto>,
            val remainingActions: RemainingActionsDto,
            val knownAbilities: Map<String, Int>,
            val learnedAbilityIds: List<String>,
            val maximumAbilities: Int,
            val ongoingEffects: List<OngoingEffectSnapshotDto>,
            val remainingSummonDuration: Int?,
        )

        /** An effect of an equipped item, with the amount its bonus adds to the maximum health or mana. */
        data class EquippedEffectSnapshotDto(
            val lootableItemId: String,
            val effectId: String,
            val bonus: BonusDto,
            val amount: Int,
        ) {
            enum class BonusDto {
                MAXIMUM_HEALTH,
                MAXIMUM_MANA,
            }
        }

        /** An effect received and not triggered yet, with its remaining duration when it triggers on each player turn started. */
        data class OngoingEffectSnapshotDto(
            val effectId: String,
            val timing: TimingDto,
            val remainingDuration: Int?,
        ) {
            enum class TimingDto {
                IMMEDIATE,
                ON_PLAYER_TURN_STARTED,
                ON_DEFEATED,
            }
        }
    }
}
