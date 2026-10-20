package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the inventory view to display, instead of drawing it.
// The player actions are simulated by calling the delegate (e.g.: `slotSelected`).
class FakeInventoryView : InventoryView(BattleSceneResourcesLoader()) {
    var delegate: Delegate? = null
        private set
    val displayedSlots = mutableListOf<List<Slot>>()
    var displayedGold: Int? = null
        private set
    var selectedSlot: Int? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(
        slots: List<Slot>,
        gold: Int,
    ) {
        displayedSlots += slots
        displayedGold = gold
        selectedSlot = null
    }

    override fun selectSlot(index: Int?) {
        selectedSlot = index
    }
}
