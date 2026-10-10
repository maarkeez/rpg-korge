package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.Battle
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository

/**
 * Comparable snapshot of the whole battle state, used to prove that an interaction (e.g.: a preview) left the game untouched.
 */
data class BattleStateFingerprint(
    val battleUnits: List<BattleUnit.Dto>,
    val positions: Map<String, Battlefield.Dto.PositionDto?>,
    val battle: Battle.Dto?,
    val battlefieldHud: BattlefieldHud?,
) {
    companion object {
        fun capture(
            playerIds: List<String>,
            battleUnitApi: BattleUnitApi,
            battlefieldApi: BattlefieldApi,
            battleApi: BattleApi,
            battlefieldHudRepository: BattlefieldHudRepository? = null,
        ): BattleStateFingerprint {
            val battleUnits = playerIds.flatMap { battleUnitApi.searchBattleUnitsByPlayerId(it) }
            return BattleStateFingerprint(
                battleUnits = battleUnits,
                positions = battleUnits.associate { it.id to battlefieldApi.searchPosition(it.id) },
                battle = battleApi.searchBattle(),
                battlefieldHud = battlefieldHudRepository?.search(),
            )
        }
    }
}
