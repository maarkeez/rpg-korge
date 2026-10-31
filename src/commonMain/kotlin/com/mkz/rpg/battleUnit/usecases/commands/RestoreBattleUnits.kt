package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnit.Dto.EquippedEffectSnapshotDto
import com.mkz.rpg.battleUnit.domain.BattleUnit.Dto.OngoingEffectSnapshotDto
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById

/**
 * Replaces every battle unit by the ones a save took, publishing no events. Fails when a battle unit names a unit, a
 * player, an ability or an effect the game no longer has.
 */
class RestoreBattleUnits(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchUnitById: SearchUnitById,
    private val searchPlayerById: SearchPlayerById,
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
) {
    operator fun invoke(battleUnits: List<BattleUnit.Dto.SnapshotDto>) {
        battleUnits.forEach(::requireKnownIds)
        battleUnitRepository.searchAll().forEach { battleUnit -> battleUnitRepository.delete(battleUnit.toDto().id) }
        battleUnits.forEach { battleUnit -> battleUnitRepository.create(BattleUnit.restore(battleUnit)) }
    }

    private fun requireKnownIds(battleUnit: BattleUnit.Dto.SnapshotDto) {
        requireNotNull(searchUnitById(battleUnit.unitId)) { "Unknown unit ${battleUnit.unitId}" }
        requireNotNull(searchPlayerById(battleUnit.playerId)) { "Unknown player ${battleUnit.playerId}" }
        battleUnit.abilityCooldowns.keys.forEach { abilityId -> requireNotNull(searchAbilityById(abilityId)) { "Unknown ability $abilityId" } }
        (battleUnit.equippedEffects.map(EquippedEffectSnapshotDto::effectId) + battleUnit.ongoingEffects.map(OngoingEffectSnapshotDto::effectId)).forEach { effectId ->
            requireNotNull(searchEffectById(effectId)) { "Unknown effect $effectId" }
        }
    }
}
