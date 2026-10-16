package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.usecases.services.AbilityExecution
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

/**
 * The effects that would stay on each battle unit (e.g.: a venom damaging it on every turn started) when the ability
 * is cast on the cast group, keyed by battle unit id. Each effect is listed as many times as it would be received.
 * The effects applied immediately are not listed, as they do not stay on the battle units.
 */
class PreviewCastOngoingEffects(
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val abilityExecution: AbilityExecution,
) {
    companion object {
        val ONGOING_APPLICATION_TYPES = setOf(ApplicationTypeDto.ON_TURN_STARTED, ApplicationTypeDto.ON_DEFEATED)
    }

    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        castGroup: List<Battlefield.Dto.PositionDto>,
    ): Map<String, List<String>> {
        val ability = searchAbilityById(abilityId) ?: return emptyMap()
        // The same applications as the cast, which applies the ability on each tile of the cast group.
        return castGroup
            .flatMap { position -> abilityExecution(casterId = battleUnitId, ability = ability, selectedRow = position.row, selectedColumn = position.column) }
            .filter { application -> searchEffectById(application.effectId)?.application?.type in ONGOING_APPLICATION_TYPES }
            .mapNotNull { application -> (application.target as? EffectTargetDto.Unit)?.let { target -> target.id to application.effectId } }
            .groupBy(keySelector = { (targetId, _) -> targetId }, valueTransform = { (_, effectId) -> effectId })
    }
}
