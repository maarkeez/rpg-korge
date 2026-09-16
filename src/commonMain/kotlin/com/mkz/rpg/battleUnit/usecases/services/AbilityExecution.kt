package com.mkz.rpg.battleUnit.usecases.services

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.TargetExpression
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToResolveEffectTarget
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.Effect.EffectApplication
import com.mkz.rpg.effect.domain.Effect.EffectTarget
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

class AbilityExecution(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val searchOccupant: SearchOccupant,
    private val searchEffectById: SearchEffectById,
    private val distanceService: DistanceService,
) {
    operator fun invoke(
        casterId: String,
        ability: Ability.Dto,
        selectedRow: Int,
        selectedColumn: Int,
    ): List<EffectApplication> {
        val caster = battleUnitRepository.searchById(casterId) ?: throw FailedToResolveEffectTarget()
        validateTargeting(ability = ability, caster = caster, selectedRow = selectedRow, selectedColumn = selectedColumn)
        return ability.effectSpecs.flatMap { effectSpec ->
            resolveTargets(
                effectSpec = effectSpec,
                ability = ability,
                caster = caster,
                selectedRow = selectedRow,
                selectedColumn = selectedColumn,
            ).map { target ->
                EffectApplication(
                    source = casterId,
                    target = target,
                    effectId = effectSpec.effectId,
                    destination = destinationFor(effectId = effectSpec.effectId, selectedRow = selectedRow, selectedColumn = selectedColumn),
                )
            }
        }
    }

    private fun validateTargeting(
        ability: Ability.Dto,
        caster: BattleUnit,
        selectedRow: Int,
        selectedColumn: Int,
    ) {
        when (ability.targeting) {
            Ability.Dto.TargetingDto.ADJACENT_ENEMY,
            Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
            -> {
                val occupantId = searchOccupant(selectedRow, selectedColumn) ?: throw FailedToResolveEffectTarget()
                val occupant = battleUnitRepository.searchById(occupantId) ?: throw FailedToResolveEffectTarget()
                if (caster.isSamePlayer(occupant)) throw FailedToResolveEffectTarget()
            }
            Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT -> {
                if (searchOccupant(selectedRow, selectedColumn) != null) throw FailedToResolveEffectTarget()
            }
            Ability.Dto.TargetingDto.SELF -> Unit
        }
    }

    private fun resolveTargets(
        effectSpec: Ability.Dto.EffectSpecDto,
        ability: Ability.Dto,
        caster: BattleUnit,
        selectedRow: Int,
        selectedColumn: Int,
    ): List<EffectTarget> =
        when (val targetExpression = TargetExpression(effectSpec.target)) {
            is TargetExpression.Caster -> listOf(EffectTarget.Unit(id = caster.toDto().id))
            is TargetExpression.SelectedTarget -> {
                val occupantId = searchOccupant(selectedRow, selectedColumn) ?: throw FailedToResolveEffectTarget()
                listOf(EffectTarget.Unit(id = occupantId))
            }
            is TargetExpression.SelectedTile -> {
                if (searchOccupant(selectedRow, selectedColumn) != null) throw FailedToResolveEffectTarget()
                listOf(EffectTarget.Tile(row = selectedRow, column = selectedColumn))
            }
            is TargetExpression.CasterTile -> {
                val position = searchPosition(caster.toDto().id) ?: throw FailedToResolveEffectTarget()
                listOf(EffectTarget.Tile(row = position.row, column = position.column))
            }
            is TargetExpression.NearbyAllies -> searchNearbyAllies(caster = caster).map { ally -> EffectTarget.Unit(id = ally.toDto().id) }
        }

    private fun destinationFor(
        effectId: String,
        selectedRow: Int,
        selectedColumn: Int,
    ): EffectTarget? {
        val effect = searchEffectById(effectId) ?: return null
        return if (effect.outcome.type == TELEPORT) {
            EffectTarget.Tile(row = selectedRow, column = selectedColumn)
        } else {
            null
        }
    }

    private fun searchNearbyAllies(caster: BattleUnit): List<BattleUnit> =
        buildList {
            val position = searchPosition(caster.toDto().id) ?: return@buildList
            for (row in position.row - 1..position.row + 1) {
                for (column in position.column - 1..position.column + 1) {
                    val distance = distanceService.manhattanDistance(fromRow = position.row, fromColumn = position.column, toRow = row, toColumn = column)
                    if (distance > 1) continue
                    val occupantId = searchOccupant(row, column) ?: continue
                    val occupant = battleUnitRepository.searchById(occupantId) ?: continue
                    if (occupant.toDto().id == caster.toDto().id) continue
                    if (!occupant.isSamePlayer(caster)) continue
                    if (occupant.isDefeated()) continue
                    add(occupant)
                }
            }
        }
}
