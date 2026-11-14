package com.mkz.tessavar.battleUnit.adapters.save

import com.mkz.tessavar.battleUnit.domain.BattleUnit
import com.mkz.tessavar.battleUnit.domain.BattleUnit.Dto.EquippedEffectSnapshotDto
import com.mkz.tessavar.battleUnit.domain.BattleUnit.Dto.OngoingEffectSnapshotDto
import com.mkz.tessavar.battleUnit.domain.BattleUnit.Dto.RemainingActionsDto
import com.mkz.tessavar.battleUnit.usecases.commands.LoadBattleUnitsSnapshot
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsSnapshot
import com.mkz.tessavar.shared.adapters.save.SnapshotJson
import com.mkz.tessavar.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class BattleUnitSnapshottable(
    private val searchBattleUnitsSnapshot: SearchBattleUnitsSnapshot,
    private val loadBattleUnitsSnapshot: LoadBattleUnitsSnapshot,
) : Snapshottable {
    override val key = "battleUnit"

    override fun snapshot(): String = SnapshotJson.encodeToString(searchBattleUnitsSnapshot().map(BattleUnitSnapshot::from))

    override fun load(snapshot: String) = loadBattleUnitsSnapshot(SnapshotJson.decodeFromString<List<BattleUnitSnapshot>>(snapshot).map(BattleUnitSnapshot::toDto))
}

private const val CASTS_OF_EVERY_PLAYER_TURN = 1

@Serializable
private data class BattleUnitSnapshot(
    val id: String,
    val unitId: String,
    val playerId: String,
    val remainingHealth: Int,
    val remainingMana: Int,
    val baseMaximumHealth: Int,
    val baseMaximumMana: Int,
    val equippedEffects: List<EquippedEffect>,
    val maximumSteps: Int,
    val remainingSteps: Int,
    val remainingCasts: Int,
    val knownAbilities: Map<String, Int>,
    val learnedAbilityIds: List<String>,
    val maximumAbilities: Int,
    val ongoingEffects: List<OngoingEffect>,
    val remainingSummonDuration: Int?,
) {
    companion object {
        fun from(dto: BattleUnit.Dto.SnapshotDto) =
            BattleUnitSnapshot(
                id = dto.id,
                unitId = dto.unitId,
                playerId = dto.playerId,
                remainingHealth = dto.remainingHealth,
                remainingMana = dto.remainingMana,
                baseMaximumHealth = dto.baseMaximumHealth,
                baseMaximumMana = dto.baseMaximumMana,
                equippedEffects = dto.equippedEffects.map { effect -> EquippedEffect(effect.lootableItemId, effect.effectId, effect.bonus.name, effect.amount) },
                maximumSteps = dto.remainingActions.maximumSteps,
                remainingSteps = dto.remainingActions.remainingSteps,
                remainingCasts = dto.remainingActions.remainingCasts,
                knownAbilities = dto.knownAbilities,
                learnedAbilityIds = dto.learnedAbilityIds,
                maximumAbilities = dto.maximumAbilities,
                ongoingEffects = dto.ongoingEffects.map { effect -> OngoingEffect(effect.effectId, effect.timing.name, effect.remainingDuration) },
                remainingSummonDuration = dto.remainingSummonDuration,
            )
    }

    fun toDto() =
        BattleUnit.Dto.SnapshotDto(
            id = id,
            unitId = unitId,
            playerId = playerId,
            remainingHealth = remainingHealth,
            remainingMana = remainingMana,
            baseMaximumHealth = baseMaximumHealth,
            baseMaximumMana = baseMaximumMana,
            equippedEffects =
                equippedEffects.map { effect ->
                    EquippedEffectSnapshotDto(effect.lootableItemId, effect.effectId, EquippedEffectSnapshotDto.BonusDto.valueOf(effect.bonus), effect.amount)
                },
            remainingActions = RemainingActionsDto(remainingCasts = remainingCasts, remainingSteps = remainingSteps, maximumSteps = maximumSteps, maximumCasts = CASTS_OF_EVERY_PLAYER_TURN),
            knownAbilities = knownAbilities,
            learnedAbilityIds = learnedAbilityIds,
            maximumAbilities = maximumAbilities,
            ongoingEffects = ongoingEffects.map { effect -> OngoingEffectSnapshotDto(effect.effectId, OngoingEffectSnapshotDto.TimingDto.valueOf(effect.timing), effect.remainingDuration) },
            remainingSummonDuration = remainingSummonDuration,
        )

    @Serializable
    data class EquippedEffect(
        val lootableItemId: String,
        val effectId: String,
        val bonus: String,
        val amount: Int,
    )

    @Serializable
    data class OngoingEffect(
        val effectId: String,
        val timing: String,
        val remainingDuration: Int?,
    )
}
