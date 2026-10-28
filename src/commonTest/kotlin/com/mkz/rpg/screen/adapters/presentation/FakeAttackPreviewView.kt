package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the attack preview view to display, instead of drawing it.
class FakeAttackPreviewView : AttackPreviewView(BattleSceneResourcesLoader(), TextsMother.texts()) {
    data class DisplayedAttackPreview(
        val casterBattleUnit: BattleUnitInfo,
        val manaCost: Int,
        val receivers: List<Receiver>,
        val casterRemainingHealthPoints: Int,
        val casterNewEffectIds: List<String>,
    )

    val displayedAttackPreviews = mutableListOf<DisplayedAttackPreview>()
    var hideCount = 0
        private set

    override fun display(
        casterBattleUnit: BattleUnitInfo,
        manaCost: Int,
        receivers: List<Receiver>,
        casterRemainingHealthPoints: Int,
        casterNewEffectIds: List<String>,
    ) {
        displayedAttackPreviews += DisplayedAttackPreview(casterBattleUnit, manaCost, receivers, casterRemainingHealthPoints, casterNewEffectIds)
    }

    override fun hide() {
        hideCount++
    }
}
