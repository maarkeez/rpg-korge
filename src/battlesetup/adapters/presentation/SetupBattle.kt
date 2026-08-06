package battlesetup.adapters.presentation

import battle.adapters.presentation.BattleApi
import player.adapters.presentation.PlayerApi
import player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN

class SetupBattle(
    private val playerApi: PlayerApi,
    private val battleApi: BattleApi,
) {
    operator fun invoke(){
        val playerOneId = "player-one"
        val playerTwoId = "player-two"
        playerApi.requestPlayerCreation(playerOneId, "Human", HUMAN)
        playerApi.requestPlayerCreation(playerTwoId, "CPU", CPU)
        battleApi.startFirstRound(listOf(playerOneId, playerTwoId))
    }
}
