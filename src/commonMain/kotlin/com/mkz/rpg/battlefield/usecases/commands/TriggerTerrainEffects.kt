package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

/** Triggers the effect of each terrain for the battle units standing on it, among the given ones (e.g.: those in the battle). */
class TriggerTerrainEffects(
    private val battlefieldRepository: BattlefieldRepository,
    private val searchTerrainById: SearchTerrainById,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitIds: Set<String>) {
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
        val applications =
            battlefield
                .effectApplicationsToOccupants(terrainEffectIds)
                .filter { application -> application.occupantBattleUnitId in battleUnitIds }
        applications.forEach { application ->
            eventBus.publish(
                BattlefieldEvent.TerrainEffectTriggered(
                    application =
                        EffectApplicationDto(
                            source = ApplicationSourceDto.terrain(application.terrainId),
                            target = EffectTargetDto.Unit(id = application.occupantBattleUnitId),
                            effectId = application.effectId,
                        ),
                ),
            )
        }
    }
}
