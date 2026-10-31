package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the battle unit info view to display, instead of drawing it.
class FakeBattleUnitInfoView : BattleUnitInfoView(BattleSceneResourcesLoader(), TextsMother.texts()) {
    data class DisplayedBattleUnit(
        val battleUnit: BattleUnitInfo,
        val turnActions: TurnActions,
        val remainingTurns: Int?,
        val abilities: List<Ability>,
    )

    var delegate: AbilityButtonView.Delegate? = null
        private set
    val displayedBattleUnits = mutableListOf<DisplayedBattleUnit>()
    val selectedAbilities = mutableListOf<Int>()
    val displayedAbilityInfos = mutableListOf<AbilityInfoView.AbilityInfo>()
    var hideAbilityInfoCount = 0
        private set
    var hideCount = 0
        private set

    // The bag the player can click, or `null` while it is hidden.
    var onBagSelected: (() -> kotlin.Unit)? = null
        private set

    override fun setDelegate(delegate: AbilityButtonView.Delegate) {
        this.delegate = delegate
    }

    override fun display(
        battleUnit: BattleUnitInfo,
        turnActions: TurnActions,
        remainingTurns: Int?,
        abilities: List<Ability>,
    ) {
        displayedBattleUnits += DisplayedBattleUnit(battleUnit, turnActions, remainingTurns, abilities)
    }

    override fun displayAbilitySelected(index: Int) {
        selectedAbilities += index
    }

    override fun displayAbilityInfo(abilityInfo: AbilityInfoView.AbilityInfo) {
        displayedAbilityInfos += abilityInfo
    }

    override fun hideAbilityInfo() {
        hideAbilityInfoCount++
    }

    override fun displayBag(onBagSelected: () -> kotlin.Unit) {
        this.onBagSelected = onBagSelected
    }

    override fun hideBag() {
        onBagSelected = null
    }

    override fun hide() {
        hideCount++
    }
}
