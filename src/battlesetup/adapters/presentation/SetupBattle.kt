package battlesetup.adapters.presentation

import battle.adapters.presentation.BattleApi
import battle.domain.Battle
import battlefield.adapters.presentation.BattlefieldApi
import player.adapters.presentation.PlayerApi
import player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN

class SetupBattle(
    private val playerApi: PlayerApi,
    private val battleApi: BattleApi,
    private val battlefieldApi: BattlefieldApi,
) {
    operator fun invoke(){
        val playerOneId = "player-one"
        val playerTwoId = "player-two"
        playerApi.requestPlayerCreation(playerOneId, "Human", HUMAN)
        playerApi.requestPlayerCreation(playerTwoId, "CPU", CPU)
        battleApi.startFirstRound(listOf(playerOneId, playerTwoId))
        battlefieldApi.initializeBattlefield(8, 8, List(8){List(8){ "tile-id-$it" }})
    }
}
