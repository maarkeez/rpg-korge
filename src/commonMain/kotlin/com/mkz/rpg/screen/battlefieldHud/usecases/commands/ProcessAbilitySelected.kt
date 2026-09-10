package com.mkz.rpg.screen.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError.InvalidBattlefieldHudState
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.shared.domain.EventBus

class ProcessAbilitySelected(
    private val canCastAbility: CanCastAbility,
    private val whereCanCast: WhereCanCast,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when (battlefieldHud) {
            is DisplayMovementRange -> {
                val canCast = canCastAbility(battlefieldHud.battleUnitId, abilityId)
                if (!canCast) return
                val tilesWhereCanCast = tilesWhereCanCast(battlefieldHud.battleUnitId, abilityId)
                val (events, updatedBattlefieldHud) =
                    battlefieldHud
                        .selectAbility(
                            abilityId = abilityId,
                            tilesWhereCanCast = tilesWhereCanCast,
                        ).pullEvents()
                battlefieldHudRepository.update(updatedBattlefieldHud)
                eventBus.publish(events)
            }

            is DisplayAbilityCastRange -> {
                if (abilityId == battlefieldHud.abilityId) {
                    val (events, updatedBattlefieldHud) = battlefieldHud.deselectAbility().pullEvents()
                    battlefieldHudRepository.update(updatedBattlefieldHud)
                    eventBus.publish(events)
                } else {
                    val tilesWhereCanCast = tilesWhereCanCast(battlefieldHud.battleUnitId, abilityId)
                    val (events, updatedBattlefieldHud) =
                        battlefieldHud
                            .selectAbility(
                                abilityId = abilityId,
                                tilesWhereCanCast = tilesWhereCanCast,
                            ).pullEvents()
                    battlefieldHudRepository.update(updatedBattlefieldHud)
                    eventBus.publish(events)
                }
            }

            is Idle,
            is DisplayAbilityCastPreview,
            -> throw InvalidBattlefieldHudState()
        }
    }

    private fun tilesWhereCanCast(
        battleUnitId: String,
        abilityId: String,
    ): Set<TileDto> {
        val castPositions = whereCanCast(battleUnitId, abilityId)
        val tilesWhereCanCast =
            castPositions
                .map { position ->
                    TileDto(row = position.row, column = position.column)
                }.toSet()
        return tilesWhereCanCast
    }
}
