package screen.battlefieldHud.usecases.commands

import battleunit.adapters.presentation.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import screen.battlefieldHud.domain.BattlefieldHudError.InvalidBattlefieldHudState
import screen.battlefieldHud.usecases.services.MovementService
import shared.domain.*

class ProcessAbilitySelected(
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is DisplayMovementRange -> {
                val canCastAbility = battleUnitApi.canCastAbility(battlefieldHud.battleUnitId, abilityId)
                if(!canCastAbility) return
                val tilesWhereCanCast = tilesWhereCanCast(battlefieldHud.battleUnitId, abilityId)
                val (events, updatedBattlefieldHud) = battlefieldHud.selectAbility(
                    abilityId = abilityId,
                    tilesWhereCanCast = tilesWhereCanCast,
                ).pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }

            is DisplayAbilityCastRange -> {
                if(abilityId == battlefieldHud.abilityId) {
                    val (events, updatedBattlefieldHud) = battlefieldHud.deselectAbility().pullEvents()
                    battlefieldHudRepository.update(updatedBattlefieldHud)
                    eventBus.publish(events)
                } else {
                    val tilesWhereCanCast = tilesWhereCanCast(battlefieldHud.battleUnitId, abilityId)
                    val (events, updatedBattlefieldHud) = battlefieldHud.selectAbility(
                        abilityId = abilityId,
                        tilesWhereCanCast = tilesWhereCanCast
                    ).pullEvents()
                    battlefieldHudRepository.update(updatedBattlefieldHud)
                    eventBus.publish(events)
                }
            }

            is Idle,
            is DisplayAbilityCastPreview -> throw InvalidBattlefieldHudState()
        }
    }

    private fun tilesWhereCanCast(
        battleUnitId: String,
        abilityId: String
    ): Set<TileDto> {
        val castPositions = battleUnitApi.whereCanCast(battleUnitId, abilityId)
        val tilesWhereCanCast = castPositions.map { position ->
            TileDto(row = position.row, column = position.column)
        }.toSet()
        return tilesWhereCanCast
    }
}
