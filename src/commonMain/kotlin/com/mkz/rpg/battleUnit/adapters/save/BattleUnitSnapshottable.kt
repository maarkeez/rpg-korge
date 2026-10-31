package com.mkz.rpg.battleUnit.adapters.save

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnit.Dto.EquippedEffectSnapshotDto
import com.mkz.rpg.battleUnit.domain.BattleUnit.Dto.OngoingEffectSnapshotDto
import com.mkz.rpg.battleUnit.domain.BattleUnit.Dto.RemainingTurnActionsDto
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Every battle unit as it is, the defeated ones left out, as they are deleted. Restoring fails when a battle unit names
 * a unit, a player, an ability or an effect the game no longer has.
 */
class BattleUnitSnapshottable(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
) : Snapshottable {
    override val key = "battleUnit"

    override fun snapshot(): String = SnapshotJson.encodeToString(battleUnitRepository.searchAll().map { battleUnit -> BattleUnitSnapshot.from(battleUnit.toSnapshot()) })

    override fun restore(snapshot: String) {
        val battleUnits = SnapshotJson.decodeFromString<List<BattleUnitSnapshot>>(snapshot).map { battleUnitSnapshot -> battleUnitSnapshot.toDto().also(::requireKnownIds) }
        battleUnitRepository.searchAll().forEach { battleUnit -> battleUnitRepository.delete(battleUnit.toDto().id) }
        battleUnits.forEach { battleUnit -> battleUnitRepository.create(BattleUnit.restore(battleUnit)) }
    }

    private fun requireKnownIds(battleUnit: BattleUnit.Dto.SnapshotDto) {
        requireNotNull(searchUnitById(battleUnit.unitId)) { "Unknown unit ${battleUnit.unitId}" }
        requireNotNull(searchPlayerById(battleUnit.playerId)) { "Unknown player ${battleUnit.playerId}" }
        battleUnit.abilityCooldowns.keys.forEach { abilityId -> requireNotNull(searchAbilityById(abilityId)) { "Unknown ability $abilityId" } }
        (battleUnit.equippedEffects.map(EquippedEffectSnapshotDto::effectId) + battleUnit.ongoingEffects.map(OngoingEffectSnapshotDto::effectId)).forEach { effectId ->
            requireNotNull(searchEffectById(effectId)) { "Unknown effect $effectId" }
        }
    }
}

@Serializable
private data class BattleUnitSnapshot(
    val id: String,
    val unitId: String,
    val playerId: String,
    val remainingHealthPoints: Int,
    val remainingManaPoints: Int,
    val baseMaximumHealthPoints: Int,
    val baseMaximumManaPoints: Int,
    val equippedEffects: List<EquippedEffect>,
    val movementRange: Int,
    val remainingSteps: Int,
    val remainingCasts: Int,
    val abilityCooldowns: Map<String, Int>,
    val learnedAbilityIds: List<String>,
    val maximumAbilities: Int,
    val ongoingEffects: List<OngoingEffect>,
    val remainingTurns: Int?,
) {
    companion object {
        fun from(dto: BattleUnit.Dto.SnapshotDto) =
            BattleUnitSnapshot(
                id = dto.id,
                unitId = dto.unitId,
                playerId = dto.playerId,
                remainingHealthPoints = dto.remainingHealthPoints,
                remainingManaPoints = dto.remainingManaPoints,
                baseMaximumHealthPoints = dto.baseMaximumHealthPoints,
                baseMaximumManaPoints = dto.baseMaximumManaPoints,
                equippedEffects = dto.equippedEffects.map { effect -> EquippedEffect(effect.lootableItemId, effect.effectId, effect.bonus.name, effect.points) },
                movementRange = dto.remainingTurnActions.maximumSteps,
                remainingSteps = dto.remainingTurnActions.remainingSteps,
                remainingCasts = dto.remainingTurnActions.remainingCasts,
                abilityCooldowns = dto.abilityCooldowns,
                learnedAbilityIds = dto.learnedAbilityIds,
                maximumAbilities = dto.maximumAbilities,
                ongoingEffects = dto.ongoingEffects.map { effect -> OngoingEffect(effect.effectId, effect.application.name, effect.turnsLeft) },
                remainingTurns = dto.remainingTurns,
            )
    }

    fun toDto() =
        BattleUnit.Dto.SnapshotDto(
            id = id,
            unitId = unitId,
            playerId = playerId,
            remainingHealthPoints = remainingHealthPoints,
            remainingManaPoints = remainingManaPoints,
            baseMaximumHealthPoints = baseMaximumHealthPoints,
            baseMaximumManaPoints = baseMaximumManaPoints,
            equippedEffects =
                equippedEffects.map { effect ->
                    EquippedEffectSnapshotDto(effect.lootableItemId, effect.effectId, EquippedEffectSnapshotDto.BonusDto.valueOf(effect.bonus), effect.points)
                },
            // The casts of a turn are not saved, as they are the same for every battle unit.
            remainingTurnActions = RemainingTurnActionsDto(remainingCasts = remainingCasts, remainingSteps = remainingSteps, maximumSteps = movementRange, maximumCasts = 1),
            abilityCooldowns = abilityCooldowns,
            learnedAbilityIds = learnedAbilityIds,
            maximumAbilities = maximumAbilities,
            ongoingEffects = ongoingEffects.map { effect -> OngoingEffectSnapshotDto(effect.effectId, OngoingEffectSnapshotDto.ApplicationDto.valueOf(effect.application), effect.turnsLeft) },
            remainingTurns = remainingTurns,
        )

    // Bonus: MAXIMUM_HEALTH or MAXIMUM_MANA.
    @Serializable
    data class EquippedEffect(
        val lootableItemId: String,
        val effectId: String,
        val bonus: String,
        val points: Int,
    )

    // Application: IMMEDIATE, ON_TURN_STARTED or ON_DEFEATED. Turns left: only when it applies every turn.
    @Serializable
    data class OngoingEffect(
        val effectId: String,
        val application: String,
        val turnsLeft: Int?,
    )
}
