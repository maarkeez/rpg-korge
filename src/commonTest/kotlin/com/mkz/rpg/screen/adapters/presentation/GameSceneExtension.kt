package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.view.Stage

fun Stage.battlefieldView(): BattlefieldView? = descendants().filterIsInstance<BattlefieldView>().firstOrNull()

fun Stage.callToActionView(): CallToActionView? = descendants().filterIsInstance<CallToActionView>().firstOrNull()

fun Stage.virtualPadView(): VirtualPadView? = descendants().filterIsInstance<VirtualPadView>().firstOrNull()

fun Stage.formationView(): FormationView? = descendants().filterIsInstance<FormationView>().firstOrNull()

fun Stage.hudView(): HudView? = descendants().filterIsInstance<HudView>().firstOrNull()

fun Stage.battleUnitInfoView(): BattleUnitInfoView? = descendants().filterIsInstance<BattleUnitInfoView>().firstOrNull()

fun Stage.inventoryView(): InventoryView? = descendants().filterIsInstance<InventoryView>().firstOrNull()

fun Stage.giveGoldView(): GiveGoldView? = descendants().filterIsInstance<GiveGoldView>().firstOrNull()

fun Stage.bannerView(): BannerView? = descendants().filterIsInstance<BannerView>().firstOrNull()

fun Stage.questTrackerView(): QuestTrackerView? = descendants().filterIsInstance<QuestTrackerView>().firstOrNull()

fun Stage.questDetailView(): QuestDetailView? = descendants().filterIsInstance<QuestDetailView>().firstOrNull()

fun Stage.talkView(): TalkView? = descendants().filterIsInstance<TalkView>().firstOrNull()

fun Stage.questJournalView(): QuestJournalView? = descendants().filterIsInstance<QuestJournalView>().firstOrNull()

fun Stage.questAnnouncementView(): QuestAnnouncementView? = descendants().filterIsInstance<QuestAnnouncementView>().firstOrNull()

fun Stage.isTurnAnnounced(): Boolean {
    val bannerView = bannerView() ?: return false
    val battlefieldView = battlefieldView() ?: return false
    return !bannerView.playerTurnText().isNullOrEmpty() &&
        bannerView.playerTurnScale() == 1.0 &&
        battlefieldView.tintColor() == null
}

fun Stage.canHumanPlayerAct(): Boolean = isTurnAnnounced() && callToActionView()?.isFinishPlayerTurnEnabled() == true

fun Stage.isHudShown(): Boolean = hudView()?.isShown() == true && callToActionView()?.isShown() == true
