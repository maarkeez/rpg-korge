package battle.adapters.presentation

import battle.adapters.events.OnRoundFinished
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
    // Storage
    private val battleRepository : BattleRepository = InMemoryBattleRepository()

    // Commands
    val defeatPlayer = DefeatPlayer(battleRepository, eventBus)
    val finishBattle = FinishBattle(battleRepository, eventBus)
    val finishPlayerTurn = FinishPlayerTurn(battleRepository, eventBus)
    val startFirstRound = StartFirstRound(battleRepository, eventBus)
    val startNextRound = StartNextRound(battleRepository, eventBus)

    // Queries
    val searchBattle = SearchBattle(battleRepository)

    // Event Listeners
    val onTurnFinished = OnRoundFinished(eventBus, startNextRound)
}
