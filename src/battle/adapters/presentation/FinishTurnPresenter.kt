package battle.adapters.presentation

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
