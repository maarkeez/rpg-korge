package com.mkz.rpg.screen.adapters.presentation

class FakeInventoryView : InventoryView(GameSceneResourcesLoader(), MessagesMother.messages()) {
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
