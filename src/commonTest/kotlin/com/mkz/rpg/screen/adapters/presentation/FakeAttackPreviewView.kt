package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.unit.domain.Unit

// Records what the presenters ask the attack preview view to display, instead of drawing it.
class FakeAttackPreviewView : AttackPreviewView(BattleSceneResourcesLoader()) {
    data class DisplayedAttackPreview(
        val casterBattleUnit: BattleUnit.Dto,
        val casterUnit: Unit.Dto,
        val manaCost: Int,
        val receiverBattleUnit: BattleUnit.Dto?,
        val receiverUnit: Unit.Dto?,
        val damage: Int?,
    )

    val displayedAttackPreviews = mutableListOf<DisplayedAttackPreview>()
    var hideCount = 0
        private set

    override fun display(
        casterBattleUnit: BattleUnit.Dto,
        casterUnit: Unit.Dto,
        manaCost: Int,
        receiverBattleUnit: BattleUnit.Dto?,
        receiverUnit: Unit.Dto?,
        damage: Int?,
    ) {
        displayedAttackPreviews += DisplayedAttackPreview(casterBattleUnit, casterUnit, manaCost, receiverBattleUnit, receiverUnit, damage)
    }

    override fun hide() {
        hideCount++
    }
}
