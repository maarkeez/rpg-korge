package battle.adapters.presentation

import battle.adapters.storage.InMemoryBattleRepository
import battle.domain.BattleRepository
import battle.usecases.commands.DefeatPlayer
import battle.usecases.commands.FinishBattle
import battle.usecases.commands.FinishPlayerTurn
import battle.usecases.commands.StartFirstRound
import battle.usecases.commands.StartNextRound
import battle.usecases.queries.SearchBattle
import shared.domain.EventBus

class BattleApi(
    eventBus: EventBus,
) {
    private val battleRepository : BattleRepository = InMemoryBattleRepository()
    val defeatPlayer = DefeatPlayer(battleRepository, eventBus)
    val finishBattle = FinishBattle(battleRepository, eventBus)
    val finishPlayerTurn = FinishPlayerTurn(battleRepository, eventBus)
    val startFirstRound = StartFirstRound(battleRepository, eventBus)
    val startNextRound = StartNextRound(battleRepository, eventBus)
    val searchBattle = SearchBattle(battleRepository)
}
