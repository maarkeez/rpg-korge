package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.ability.usecases.queries.SearchAbilityById
import com.mkz.tessavar.battleUnit.usecases.services.CastResolution
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById

class PreviewCastOngoingEffects(
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val castResolution: CastResolution,
) {
    companion object {
        val ONGOING_TIMINGS = setOf(TimingTypeDto.ON_PLAYER_TURN_STARTED, TimingTypeDto.ON_DEFEATED)
    }

    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        castGroup: List<Battlefield.Dto.PositionDto>,
    ): Map<String, List<String>> {
        val ability = searchAbilityById(abilityId) ?: return emptyMap()
        return castGroup
            .flatMap { position -> castResolution(casterId = battleUnitId, ability = ability, targetedRow = position.row, targetedColumn = position.column) }
            .filter { application -> searchEffectById(application.effectId)?.timing?.type in ONGOING_TIMINGS }
            .mapNotNull { application -> application.recipient.battleUnitId?.let { battleUnitId -> battleUnitId to application.effectId } }
            .groupBy(keySelector = { (targetId, _) -> targetId }, valueTransform = { (_, effectId) -> effectId })
    }
}
