package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the panel to display, instead of drawing it.
// The player actions are simulated by calling the delegate (e.g.: `amountChanged`).
class FakeGiveGoldView : GiveGoldView(BattleSceneResourcesLoader(), TextsMother.texts()) {
    data class Displayed(
        val giver: Member,
        val receiver: Member,
        val amount: Int,
        val maximum: Int,
        val canStepToPrevious: Boolean,
        val canStepToNext: Boolean,
    )

    var delegate: Delegate? = null
        private set
    val displays = mutableListOf<Displayed>()

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(
        giver: Member,
        receiver: Member,
        amount: Int,
        maximum: Int,
        canStepToPrevious: Boolean,
        canStepToNext: Boolean,
    ) {
        displays += Displayed(giver, receiver, amount, maximum, canStepToPrevious, canStepToNext)
    }
}
