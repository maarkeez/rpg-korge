package battlesetup.adapters.presentation

import battle.adapters.presentation.BattleApi
import battlefield.adapters.presentation.BattlefieldApi
import battlesetup.usecases.commands.SetupBattle
import effect.adapters.presentation.EffectApi
import player.adapters.presentation.PlayerApi
import player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN

class BattleSetupApi(
    playerApi: PlayerApi,
    battleApi: BattleApi,
    effectApi: EffectApi,
    battlefieldApi: BattlefieldApi,
) {
    val setupBattle = SetupBattle(
        playerApi.requestPlayerCreation,
        battlefieldApi.initializeBattlefield,
        battleApi.startFirstRound,
        effectApi.requestEffectCreation
    )
}
