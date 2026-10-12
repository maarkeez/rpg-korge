package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.unit.domain.Unit

// Records what the presenters ask the battle unit info view to display, instead of drawing it.
class FakeBattleUnitInfoView : BattleUnitInfoView(BattleSceneResourcesLoader()) {
    data class DisplayedBattleUnit(
        val battleUnit: BattleUnit.Dto,
        val unit: Unit.Dto,
    )

    var delegate: AbilityButtonView.Delegate? = null
        private set
    val displayedBattleUnits = mutableListOf<DisplayedBattleUnit>()
    val selectedAbilities = mutableListOf<Int>()
    var hideCount = 0
        private set

    override fun setDelegate(delegate: AbilityButtonView.Delegate) {
        this.delegate = delegate
    }

    override fun display(
        battleUnit: BattleUnit.Dto,
        unit: Unit.Dto,
    ) {
        displayedBattleUnits += DisplayedBattleUnit(battleUnit, unit)
    }

    override fun displayAbilitySelected(index: Int) {
        selectedAbilities += index
    }

    override fun hide() {
        hideCount++
    }
}
