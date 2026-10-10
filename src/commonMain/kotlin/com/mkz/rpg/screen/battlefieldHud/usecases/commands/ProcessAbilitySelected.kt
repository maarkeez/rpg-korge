package com.mkz.rpg.screen.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastPreview
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayAbilityCastRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.DisplayMovementRange
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud.Idle
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError.BattlefieldHudNotFound
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudError.InvalidBattlefieldHudState
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudEvent.AbilityUnavailable
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.shared.domain.EventBus

class ProcessAbilitySelected(
    private val searchAbilityAvailability: SearchAbilityAvailability,
    private val whereCanCast: WhereCanCast,
    private val battlefieldHudRepository: BattlefieldHudRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(abilityId: String) {
        val battlefieldHud = battlefieldHudRepository.search() ?: throw BattlefieldHudNotFound()
        when (battlefieldHud) {
            is DisplayMovementRange -> {
                val availability = availability(battlefieldHud.battleUnitId, abilityId) ?: return
                if (availability.status != AbilityAvailability.Status.READY) {
                    eventBus.publish(unavailable(availability))
                    return
                }
                val castGroupsWhereCanCast = castGroupsWhereCanCast(battlefieldHud.battleUnitId, abilityId)
                val (events, updatedBattlefieldHud) =
                    battlefieldHud
                        .selectAbility(
                            abilityId = abilityId,
                            castGroupsWhereCanCast = castGroupsWhereCanCast,
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
                    val availability = availability(battlefieldHud.battleUnitId, abilityId)
                    if (availability != null && availability.status != AbilityAvailability.Status.READY) {
                        eventBus.publish(unavailable(availability))
                        return
                    }
                    val castGroupsWhereCanCast = castGroupsWhereCanCast(battlefieldHud.battleUnitId, abilityId)
                    val (events, updatedBattlefieldHud) =
                        battlefieldHud
                            .selectAbility(
                                abilityId = abilityId,
                                castGroupsWhereCanCast = castGroupsWhereCanCast,
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

    private fun availability(
        battleUnitId: String,
        abilityId: String,
    ): AbilityAvailability? = searchAbilityAvailability(battleUnitId).firstOrNull { it.abilityId == abilityId }

    private fun unavailable(availability: AbilityAvailability) =
        AbilityUnavailable(
            abilityId = availability.abilityId,
            reason =
                when (availability.status) {
                    AbilityAvailability.Status.NO_CASTS_LEFT -> AbilityUnavailable.Reason.NoCastsLeft
                    AbilityAvailability.Status.COOLDOWN -> AbilityUnavailable.Reason.OnCooldown(availability.cooldownTurnsLeft)
                    AbilityAvailability.Status.NOT_ENOUGH_MANA -> AbilityUnavailable.Reason.NotEnoughMana(availability.cost)
                    AbilityAvailability.Status.READY -> error("A ready ability is available")
                },
        )

    private fun castGroupsWhereCanCast(
        battleUnitId: String,
        abilityId: String,
    ): List<Dto.CastGroupDto> {
        val castGroups = whereCanCast(battleUnitId, abilityId)
        return castGroups
            .map { castGroup ->
                Dto.CastGroupDto(
                    tiles =
                        castGroup.positions
                            .map { position -> TileDto(row = position.row, column = position.column) },
                )
            }.distinct()
    }
}
