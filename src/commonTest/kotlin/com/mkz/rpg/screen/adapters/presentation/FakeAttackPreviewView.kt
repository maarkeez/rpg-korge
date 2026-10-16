package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.unit.domain.Unit

// Records what the presenters ask the attack preview view to display, instead of drawing it.
class FakeAttackPreviewView : AttackPreviewView(BattleSceneResourcesLoader()) {
    data class DisplayedAttackPreview(
        val casterBattleUnit: BattleUnit.Dto,
        val casterUnit: Unit.Dto,
        val manaCost: Int,
        val receivers: List<Receiver>,
        val casterRemainingHealthPoints: Int,
        val casterNewEffectIds: List<String>,
    )

    val displayedAttackPreviews = mutableListOf<DisplayedAttackPreview>()
    var hideCount = 0
        private set

    override fun display(
        casterBattleUnit: BattleUnit.Dto,
        casterUnit: Unit.Dto,
        manaCost: Int,
        receivers: List<Receiver>,
        casterRemainingHealthPoints: Int,
        casterNewEffectIds: List<String>,
    ) {
        displayedAttackPreviews += DisplayedAttackPreview(casterBattleUnit, casterUnit, manaCost, receivers, casterRemainingHealthPoints, casterNewEffectIds)
    }

    override fun hide() {
        hideCount++
    }
}
