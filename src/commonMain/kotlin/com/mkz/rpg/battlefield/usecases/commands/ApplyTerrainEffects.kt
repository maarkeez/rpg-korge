package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class ApplyTerrainEffects(
    private val battlefieldRepository: BattlefieldRepository,
    private val searchTerrainById: SearchTerrainById,
    private val eventBus: EventBus,
) {
    operator fun invoke(occupantIds: Set<String>) {
        val battlefield = battlefieldRepository.search() ?: return
        val terrainEffectIds =
            battlefield
                .toDto()
                .tiles
                .values
                .map { it.terrainId }
                .distinct()
                .associateWith { terrainId -> searchTerrainById(terrainId)?.effectId }
                .filterValues { it != null }
                .mapValues { it.value!! }
        val terrainEffects =
            battlefield
                .terrainEffectsOnOccupants(terrainEffectIds)
                .filter { terrainEffect -> terrainEffect.occupantId in occupantIds }
        terrainEffects.forEach { terrainEffect ->
            eventBus.publish(
                BattlefieldEvent.TerrainEffectApplied(
                    application =
                        EffectApplicationDto(
                            source = SourceDto.terrain(terrainEffect.terrainId),
                            recipient = RecipientDto.battleUnit(terrainEffect.occupantId),
                            effectId = terrainEffect.effectId,
                            destination = null,
                        ),
                ),
            )
        }
    }
}
