package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the inventory view to display, instead of drawing it.
// The player actions are simulated by calling the delegate (e.g.: `slotSelected`).
class FakeInventoryView : InventoryView(BattleSceneResourcesLoader(), TextsMother.texts()) {
    var delegate: Delegate? = null
        private set
    val displayedSlots = mutableListOf<List<Slot>>()
    var displayedGold: Int? = null
        private set
    var displayedOwner: Owner? = null
        private set
    var selectedSlot: Int? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(
        slots: List<Slot>,
        gold: Int,
        owner: Owner?,
    ) {
        displayedSlots += slots
        displayedGold = gold
        displayedOwner = owner
        selectedSlot = null
    }

    override fun selectSlot(index: Int?) {
        selectedSlot = index
    }
}
