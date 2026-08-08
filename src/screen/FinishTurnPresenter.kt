package screen

import battle.adapters.presentation.BattleApi

class FinishTurnPresenter(
    finishTurnView: FinishTurnView,
    private val battleApi: BattleApi,
) : FinishTurnView.Delegate {

    init {
        finishTurnView.setDelegate(this)
    }

    override fun finishTurn() {
        battleApi.finishPlayerTurn()
    }
}
