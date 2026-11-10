package com.mkz.rpg.screen.adapters.presentation

class FakeBattleUnitInfoView : BattleUnitInfoView(GameSceneResourcesLoader(), MessagesMother.messages()) {
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

    var onInventoryButtonSelected: (() -> kotlin.Unit)? = null
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

    override fun displayInventoryButton(onInventoryButtonSelected: () -> kotlin.Unit) {
        this.onInventoryButtonSelected = onInventoryButtonSelected
    }

    override fun hideInventoryButton() {
        onInventoryButtonSelected = null
    }

    override fun hide() {
        hideCount++
    }
}
