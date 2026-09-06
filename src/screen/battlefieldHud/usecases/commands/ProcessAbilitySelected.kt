package screen.battlefieldHud.usecases.commands

import battleunit.adapters.presentation.*
import screen.battlefieldHud.domain.*
import screen.battlefieldHud.domain.BattlefieldHud.*
import screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import screen.battlefieldHud.domain.BattlefieldHudError.InvalidBattlefieldHudState
import shared.domain.*

class ProcessAbilitySelected(
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when(battlefieldHud) {
            is Idle -> throw InvalidBattlefieldHudState()

            is DisplayMovementRange -> {
                val canCastAbility = battleUnitApi.canCastAbility(battlefieldHud.battleUnitId, abilityId)
                if(!canCastAbility) return
                val castPositions = battleUnitApi.whereCanCast(battlefieldHud.battleUnitId, abilityId)
                val tilesWhereCanCast = castPositions.map { position ->
                    TileDto(row = position.row, column = position.column)
                }.toSet()
                val (events, updatedBattlefieldHud) = battlefieldHud.selectAbility(
                    abilityId = abilityId,
                    tilesWhereCanCast = tilesWhereCanCast,
                ).pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }

            is DisplayAbilityCastRange -> {
                if(abilityId != battlefieldHud.abilityId) return
                val (events, updatedBattlefieldHud) = battlefieldHud.deselectAbility().pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }
        }
    }
}
