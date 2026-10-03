package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById

class RequestEffectApplicationToOccupants(
    private val battlefieldRepository: BattlefieldRepository,
    private val searchTerrainById: SearchTerrainById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
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
        val applications = battlefield.effectApplicationsToOccupants(terrainEffectIds)
        applications.forEach { application ->
            eventBus.publish(
                BattleUnitEvent.RequestApplyEffect(
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
