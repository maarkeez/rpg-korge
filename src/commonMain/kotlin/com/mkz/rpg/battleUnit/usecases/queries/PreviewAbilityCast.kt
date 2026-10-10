package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import com.mkz.rpg.unit.domain.Unit as UnitDomain

/**
 * What casting an ability on a cast group would do, computed without changing anything.
 *
 * It resolves the same effect applications as the real cast ([AbilityExecution]) and applies the immediate ones
 * to local copies of the battle units. Nothing is stored, published or randomized.
 */
class PreviewAbilityCast(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val searchUnitById: SearchUnitById,
    private val searchPosition: SearchPosition,
    private val abilityExecution: AbilityExecution,
) {
    operator fun invoke(
        casterId: String,
        abilityId: String,
        castGroup: List<PositionDto>,
    ): AbilityCastPreview {
        val caster = battleUnitRepository.searchById(casterId) ?: throw BattleUnitDoesNotExists()
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        val workingCopies = linkedMapOf<String, WorkingCopy>()
        val tiles = mutableListOf<TilePreview>()
        val unsupported = mutableListOf<String>()
        castGroup.forEach { position ->
            val applications = abilityExecution(casterId = casterId, ability = ability, selectedRow = position.row, selectedColumn = position.column)
            applications.forEach { application ->
                val effect = searchEffectById(application.effectId)
                when {
                    effect == null -> unsupported += "Unknown effect ${application.effectId}"
                    application.target is EffectTargetDto.Tile -> previewOnTile(application.target, effect, tiles, unsupported)
                    application.target is EffectTargetDto.Unit -> previewOnUnit(application, application.target, effect, workingCopies, unsupported)
                }
            }
        }
        val casterDto = caster.toDto()
        return AbilityCastPreview(
            casterId = casterId,
            manaBefore = casterDto.remainingManaPoints,
            manaAfter = casterDto.remainingManaPoints - ability.cost,
            cooldownAfter = ability.cooldown,
            targets = workingCopies.map { (battleUnitId, copy) -> toTargetPreview(battleUnitId, copy) },
            tiles = tiles,
            unsupported = unsupported,
        )
    }

    private fun previewOnTile(
        target: EffectTargetDto.Tile,
        effect: Effect.Dto,
        tiles: MutableList<TilePreview>,
        unsupported: MutableList<String>,
    ) {
        if (effect.outcome.type == DEPLOY_BATTLE_UNIT) {
            tiles += TilePreview(row = target.row, column = target.column, deploysUnitId = effect.outcome.deployBattleUnit?.unitId)
        } else {
            unsupported += "${effect.id} on a tile"
        }
    }

    private fun previewOnUnit(
        application: EffectApplicationDto,
        target: EffectTargetDto.Unit,
        effect: Effect.Dto,
        workingCopies: MutableMap<String, WorkingCopy>,
        unsupported: MutableList<String>,
    ) {
        val workingCopy = workingCopies[target.id] ?: loadWorkingCopy(target.id)
        if (workingCopy == null) {
            unsupported += "${effect.id} on an unknown unit"
            return
        }
        when (effect.application.type) {
            ApplicationTypeDto.IMMEDIATELY -> previewImmediate(application, effect, workingCopy, target.id, workingCopies, unsupported)
            ApplicationTypeDto.ON_TURN_STARTED -> {
                val damage = effect.outcome.decreaseHealth?.damage
                if (effect.outcome.type != DECREASE_HEALTH || damage == null) {
                    unsupported += "${effect.id} over time"
                    return
                }
                workingCopies[target.id] =
                    workingCopy.withEffect(
                        AppliedEffectPreview(
                            effectId = effect.id,
                            timing = AppliedEffectPreview.Timing.OVER_TIME,
                            perTurn = damage,
                            turns = effect.application.onTurnStarted?.duration,
                        ),
                    )
            }
            ApplicationTypeDto.ON_DEFEATED ->
                workingCopies[target.id] =
                    workingCopy.withEffect(AppliedEffectPreview(effectId = effect.id, timing = AppliedEffectPreview.Timing.ON_DEATH, perTurn = null, turns = null))
            ApplicationTypeDto.BEFORE_APPLYING_EFFECT -> unsupported += "${effect.id} before applying effects"
        }
    }

    private fun previewImmediate(
        application: EffectApplicationDto,
        effect: Effect.Dto,
        workingCopy: WorkingCopy,
        targetId: String,
        workingCopies: MutableMap<String, WorkingCopy>,
        unsupported: MutableList<String>,
    ) {
        if (effect.outcome.type !in IMMEDIATE_SUPPORTED_OUTCOMES) {
            unsupported += "${effect.id} (${effect.outcome.type})"
            return
        }
        val position = searchPosition(targetId)
        val applied =
            workingCopy.battleUnit
                .receiveImmediateEffect(effect.id)
                .applyImmediateEffect(
                    effect = effect,
                    unit = workingCopy.unit,
                    currentRow = position?.row ?: 0,
                    currentColumn = position?.column ?: 0,
                )
        val destination = (application.destination as? EffectTargetDto.Tile)?.takeIf { effect.outcome.type == TELEPORT }
        workingCopies[targetId] =
            workingCopy
                .copy(battleUnit = applied, teleportTo = destination?.let { PositionDto(it.row, it.column) } ?: workingCopy.teleportTo)
                .withEffect(AppliedEffectPreview(effectId = effect.id, timing = AppliedEffectPreview.Timing.IMMEDIATE, perTurn = null, turns = null))
    }

    private fun WorkingCopy.withEffect(effect: AppliedEffectPreview) = copy(effects = effects + effect)

    private fun toTargetPreview(
        battleUnitId: String,
        workingCopy: WorkingCopy,
    ) = TargetPreview(
        battleUnitId = battleUnitId,
        hpBefore = workingCopy.hpBefore,
        hpAfter = workingCopy.battleUnit.toDto().remainingHealthPoints,
        maxHp = workingCopy.unit.healthPoints,
        isLethal = workingCopy.hpBefore > 0 && workingCopy.battleUnit.isDefeated(),
        appliedEffects = workingCopy.effects,
        teleportTo = workingCopy.teleportTo,
    )

    private fun loadWorkingCopy(battleUnitId: String): WorkingCopy? {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return null
        val unit = searchUnitById(battleUnit.toDto().unitId) ?: return null
        return WorkingCopy(battleUnit = battleUnit, unit = unit, hpBefore = battleUnit.toDto().remainingHealthPoints)
    }

    private data class WorkingCopy(
        val battleUnit: BattleUnit,
        val unit: UnitDomain.Dto,
        val hpBefore: Int,
        val effects: List<AppliedEffectPreview> = emptyList(),
        val teleportTo: PositionDto? = null,
    )

    data class AbilityCastPreview(
        val casterId: String,
        val manaBefore: Int,
        val manaAfter: Int,
        val cooldownAfter: Int,
        val targets: List<TargetPreview>,
        val tiles: List<TilePreview>,
        /** Descriptions of the effects that can't be previewed. The UI shows them as "?" lines. */
        val unsupported: List<String>,
    )

    data class TargetPreview(
        val battleUnitId: String,
        val hpBefore: Int,
        val hpAfter: Int,
        val maxHp: Int,
        val isLethal: Boolean,
        val appliedEffects: List<AppliedEffectPreview>,
        val teleportTo: PositionDto?,
    )

    data class AppliedEffectPreview(
        val effectId: String,
        val timing: Timing,
        val perTurn: Int?,
        val turns: Int?,
    ) {
        enum class Timing { IMMEDIATE, OVER_TIME, ON_DEATH }
    }

    data class TilePreview(
        val row: Int,
        val column: Int,
        val deploysUnitId: String?,
    )

    private companion object {
        val IMMEDIATE_SUPPORTED_OUTCOMES = setOf(DECREASE_HEALTH, INCREASE_HEALTH, TELEPORT)
    }
}
