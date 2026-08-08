package battlesetup.adapters.presentation

import ability.adapters.presentation.AbilityApi
import battle.adapters.presentation.BattleApi
import battlefield.adapters.presentation.BattlefieldApi
import battlesetup.usecases.commands.SetupBattle
import battleunit.adapters.presentation.BattleUnitApi
import effect.adapters.presentation.EffectApi
import player.adapters.presentation.PlayerApi
import unit.adapters.presentation.UnitApi

class BattleSetupApi(
    playerApi: PlayerApi,
    battleApi: BattleApi,
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    unitApi: UnitApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
) {
    val setupBattle = SetupBattle(
        playerApi.requestPlayerCreation,
        battlefieldApi.initializeBattlefield,
        battleApi.startFirstRound,
        effectApi.requestEffectCreation,
        abilityApi.requestAbilityCreation,
        unitApi.requestUnitCreation,
        battleUnitApi.deployBattleUnit,
    )
}
