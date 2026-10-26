package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities.AbilityAvailability
import com.mkz.rpg.unit.domain.Unit

// Records what the presenters ask the battle unit info view to display, instead of drawing it.
class FakeBattleUnitInfoView : BattleUnitInfoView(BattleSceneResourcesLoader(), TextsMother.texts()) {
    data class DisplayedBattleUnit(
        val battleUnit: BattleUnit.Dto,
        val unit: Unit.Dto,
        val abilityAvailabilities: List<AbilityAvailability>,
    )

    var delegate: AbilityButtonView.Delegate? = null
        private set
    val displayedBattleUnits = mutableListOf<DisplayedBattleUnit>()
    val selectedAbilities = mutableListOf<Int>()
    var hideCount = 0
        private set

    // The bag the player can click, or `null` while it is hidden.
    var onBagSelected: (() -> kotlin.Unit)? = null
        private set

    override fun setDelegate(delegate: AbilityButtonView.Delegate) {
        this.delegate = delegate
    }

    override fun display(
        battleUnit: BattleUnit.Dto,
        unit: Unit.Dto,
        abilityAvailabilities: List<AbilityAvailability>,
    ) {
        displayedBattleUnits += DisplayedBattleUnit(battleUnit, unit, abilityAvailabilities)
    }

    override fun displayAbilitySelected(index: Int) {
        selectedAbilities += index
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
