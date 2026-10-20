package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefield
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

/**
 * The health its terrain gives to the battle unit standing on the tile: positive when it heals (e.g.: water), negative
 * when it hurts (e.g.: lava), and 0 otherwise.
 */
class SearchTerrainHealthEffect(
    private val searchBattlefield: SearchBattlefield,
    private val searchTerrainById: SearchTerrainById,
    private val searchEffectById: SearchEffectById,
) {
    operator fun invoke(
        row: Int,
        column: Int,
    ): Int {
        val terrainId = searchBattlefield()?.tiles?.get(PositionDto(row = row, column = column))?.terrainId ?: return 0
        val effectId = searchTerrainById(terrainId)?.effectId ?: return 0
        val effect = searchEffectById(effectId) ?: return 0
        return when (effect.outcome.type) {
            INCREASE_HEALTH -> effect.outcome.increaseHealth!!.healing
            DECREASE_HEALTH -> -effect.outcome.decreaseHealth!!.damage
            else -> 0
        }
    }
}
