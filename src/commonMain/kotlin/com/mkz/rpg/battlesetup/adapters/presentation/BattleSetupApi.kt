package com.mkz.rpg.battlesetup.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.usecases.commands.SetupBattle
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class BattleSetupApi(
    playerApi: PlayerApi,
    battleApi: BattleApi,
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    unitApi: UnitApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
) {
    val setupBattle =
        SetupBattle(
            playerApi.requestPlayerCreation,
            battlefieldApi.initializeBattlefield,
            battleApi.startFirstRound,
            effectApi.requestEffectCreation,
            abilityApi.requestAbilityCreation,
            unitApi.requestUnitCreation,
            battleUnitApi.deployBattleUnit,
        )
}
