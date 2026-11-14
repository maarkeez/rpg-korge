package com.mkz.tessavar.cpuBattleBrain.usecases.queries

import com.mkz.tessavar.battlefield.usecases.queries.SearchTerrainId
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DAMAGE
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.HEAL
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById

class SearchTerrainHealthEffect(
    private val searchTerrainId: SearchTerrainId,
    private val searchTerrainById: SearchTerrainById,
    private val searchEffectById: SearchEffectById,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): Int {
        val terrainId = searchTerrainId(row = row, column = column) ?: return 0
        val effectId = searchTerrainById(terrainId)?.effectId ?: return 0
        val effect = searchEffectById(effectId) ?: return 0
        return when (effect.outcome.type) {
            HEAL -> effect.outcome.heal!!.healing
            DAMAGE -> -effect.outcome.damage!!.damage
            else -> 0
        }
    }
}
