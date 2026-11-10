package com.mkz.rpg.screen.adapters.presentation

class FakeGiveGoldView : GiveGoldView(GameSceneResourcesLoader(), MessagesMother.messages()) {
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
