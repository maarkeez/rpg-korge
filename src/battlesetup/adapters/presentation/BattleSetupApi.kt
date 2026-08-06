package battlesetup.adapters.presentation

import ability.adapters.presentation.AbilityApi
import battle.adapters.presentation.BattleApi
import battlefield.adapters.presentation.BattlefieldApi
import battlesetup.usecases.commands.SetupBattle
import battleunit.adapters.presentation.BattleUnitApi
import effect.adapters.presentation.EffectApi
import player.adapters.presentation.PlayerApi
import player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN

class BattleSetupApi(
    playerApi: PlayerApi,
    battleApi: BattleApi,
    effectApi: EffectApi,
    abilityApi: AbilityApi,
    battleUnitApi: BattleUnitApi,
    battlefieldApi: BattlefieldApi,
) {
    val setupBattle = SetupBattle(
        playerApi.requestPlayerCreation,
        battlefieldApi.initializeBattlefield,
        battleApi.startFirstRound,
        effectApi.requestEffectCreation,
        abilityApi.requestAbilityCreation,
        battleUnitApi.deployBattleUnit,
    )
}
