package com.mkz.rpg.battleUnit.usecases.services

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.ADJACENT_ALLIES
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.CASTER_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_TILE
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToResolveEffectTarget
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

class CastResolution(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val searchOccupant: SearchOccupant,
    private val searchEffectById: SearchEffectById,
    private val adjacencyService: AdjacencyService,
) {
    operator fun invoke(
        casterId: String,
        ability: Ability.Dto,
        targetedRow: Int,
        targetedColumn: Int,
    ): List<EffectApplicationDto> {
        val caster = battleUnitRepository.searchById(casterId) ?: throw FailedToResolveEffectTarget()
        validateTargeting(ability = ability, caster = caster, targetedRow = targetedRow, targetedColumn = targetedColumn)
        return ability.abilityEffects.flatMap { abilityEffect ->
            resolveRecipients(
                abilityEffect = abilityEffect,
                caster = caster,
                targetedRow = targetedRow,
                targetedColumn = targetedColumn,
            ).map { recipient ->
                EffectApplicationDto(
                    source = EffectApplicationDto.SourceDto.battleUnit(battleUnitId = casterId, abilityId = ability.id),
                    recipient = recipient,
                    effectId = abilityEffect.effectId,
                    destination = destinationFor(effectId = abilityEffect.effectId, targetedRow = targetedRow, targetedColumn = targetedColumn),
                )
            }
        }
    }

    private fun validateTargeting(
        ability: Ability.Dto,
        caster: BattleUnit,
        targetedRow: Int,
        targetedColumn: Int,
    ) {
        when (ability.targeting) {
            Ability.Dto.TargetingDto.ADJACENT_ENEMY,
            Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
            -> {
                val occupantId = searchOccupant(targetedRow, targetedColumn) ?: throw FailedToResolveEffectTarget()
                val occupant = battleUnitRepository.searchById(occupantId) ?: throw FailedToResolveEffectTarget()
                if (caster.isSamePlayer(occupant)) throw FailedToResolveEffectTarget()
            }
            Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT,
            Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER,
            -> {
                if (searchOccupant(targetedRow, targetedColumn) != null) throw FailedToResolveEffectTarget()
            }
            Ability.Dto.TargetingDto.CASTER -> Unit
        }
    }

    private fun resolveRecipients(
        abilityEffect: Ability.Dto.AbilityEffectDto,
        caster: BattleUnit,
        targetedRow: Int,
        targetedColumn: Int,
    ): List<RecipientDto> =
        when (abilityEffect.target.type) {
            CASTER -> listOf(RecipientDto.battleUnit(caster.toDto().id))
            TARGETED_BATTLE_UNIT -> {
                val occupantId = searchOccupant(targetedRow, targetedColumn) ?: throw FailedToResolveEffectTarget()
                listOf(RecipientDto.battleUnit(occupantId))
            }
            TARGETED_TILE -> {
                if (searchOccupant(targetedRow, targetedColumn) != null) throw FailedToResolveEffectTarget()
                listOf(RecipientDto.position(row = targetedRow, column = targetedColumn))
            }
            CASTER_TILE -> {
                val position = searchPosition(caster.toDto().id) ?: throw FailedToResolveEffectTarget()
                listOf(RecipientDto.position(row = position.row, column = position.column))
            }
            ADJACENT_ALLIES -> searchAdjacentAllies(caster = caster).map { ally -> RecipientDto.battleUnit(ally.toDto().id) }
        }

    private fun destinationFor(
        effectId: String,
        targetedRow: Int,
        targetedColumn: Int,
    ): RecipientDto? {
        val effect = searchEffectById(effectId) ?: return null
        return if (effect.outcome.type == TELEPORT) {
            RecipientDto.position(row = targetedRow, column = targetedColumn)
        } else {
            null
        }
    }

    private fun searchAdjacentAllies(caster: BattleUnit): List<BattleUnit> =
        buildList {
            val position = searchPosition(caster.toDto().id) ?: return@buildList
            for (row in position.row - 1..position.row + 1) {
                for (column in position.column - 1..position.column + 1) {
                    if (!adjacencyService.isAdjacent(fromRow = position.row, fromColumn = position.column, toRow = row, toColumn = column)) continue
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
