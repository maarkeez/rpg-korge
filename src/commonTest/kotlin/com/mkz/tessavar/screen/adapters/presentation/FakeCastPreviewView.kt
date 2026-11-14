package com.mkz.tessavar.screen.adapters.presentation

class FakeCastPreviewView : CastPreviewView(GameSceneResourcesLoader(), MessagesMother.messages()) {
    data class DisplayedCastPreview(
        val abilityId: String,
        val casterBattleUnit: BattleUnitInfo,
        val manaCost: Int,
        val recipients: List<Recipient>,
        val casterRemainingHealth: Int,
        val casterNewEffectIds: List<String>,
    )

    val displayedCastPreviews = mutableListOf<DisplayedCastPreview>()
    var hideCount = 0
        private set

    override fun display(
        abilityId: String,
        casterBattleUnit: BattleUnitInfo,
        manaCost: Int,
        recipients: List<Recipient>,
        casterRemainingHealth: Int,
        casterNewEffectIds: List<String>,
    ) {
        displayedCastPreviews += DisplayedCastPreview(abilityId, casterBattleUnit, manaCost, recipients, casterRemainingHealth, casterNewEffectIds)
    }

    override fun hide() {
        hideCount++
    }
}
