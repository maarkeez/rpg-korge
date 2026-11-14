package com.mkz.tessavar.battlefield.usecases.commands

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldRepository
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.terrain.usecases.queries.SearchTerrainById

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
