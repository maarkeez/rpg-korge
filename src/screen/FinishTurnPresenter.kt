package screen

import battle.adapters.presentation.*
import battle.domain.BattleEvent.PlayerVictory
import shared.domain.*

class FinishTurnPresenter(
    private val finishTurnView: FinishTurnView,
    private val battleApi: BattleApi,
    eventBus: EventBus,
) : FinishTurnView.Delegate {

    private val subscriptions = listOf(
        eventBus.subscribe<PlayerVictory> { event ->
            hideFinishTurn()
        }
    )

    init {
        finishTurnView.setDelegate(this)
    }

    override fun finishTurn() {
        battleApi.finishPlayerTurn()
    }

    fun hideFinishTurn() {
        finishTurnView.hide()
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
