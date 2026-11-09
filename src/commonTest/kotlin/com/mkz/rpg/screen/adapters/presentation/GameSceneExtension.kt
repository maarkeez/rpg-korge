package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.view.Stage

// Page object of [GameScene], used by the end-to-end tests to find the scene views on the stage.

/** The battlefield on the screen, or `null` while the game screen is not on the stage yet. */
fun Stage.battlefieldView(): BattlefieldView? = descendants().filterIsInstance<BattlefieldView>().firstOrNull()

/** The call to action on the screen, or `null` while the game screen is not on the stage yet. */
fun Stage.callToActionView(): CallToActionView? = descendants().filterIsInstance<CallToActionView>().firstOrNull()

/** The virtual pad on the screen, or `null` while the game screen is not on the stage yet. */
fun Stage.virtualPadView(): VirtualPadView? = descendants().filterIsInstance<VirtualPadView>().firstOrNull()

/** The party formation of the game screen, or `null` when the scene is not displayed yet. */
fun Stage.formationView(): FormationView? = descendants().filterIsInstance<FormationView>().firstOrNull()

/** The HUD on the screen, or `null` while the game screen is not on the stage yet. */
fun Stage.hudView(): HudView? = descendants().filterIsInstance<HudView>().firstOrNull()

/** The selected battle unit info on the screen, or `null` while the game screen is not on the stage yet. */
fun Stage.battleUnitInfoView(): BattleUnitInfoView? = descendants().filterIsInstance<BattleUnitInfoView>().firstOrNull()

/** The inventory of the selected battle unit, or `null` while the HUD does not display it. */
fun Stage.inventoryView(): InventoryView? = descendants().filterIsInstance<InventoryView>().firstOrNull()

/** The panel to give gold, or `null` while the HUD does not display it. */
fun Stage.giveGoldView(): GiveGoldView? = descendants().filterIsInstance<GiveGoldView>().firstOrNull()

/** The banner on the screen, or `null` while the game screen is not on the stage yet. */
fun Stage.bannerView(): BannerView? = descendants().filterIsInstance<BannerView>().firstOrNull()

/** The tracker of the main quest on the screen, or `null` while the game screen is not on the stage yet. */
fun Stage.questTrackerView(): QuestTrackerView? = descendants().filterIsInstance<QuestTrackerView>().firstOrNull()

/** The detail of the main quest of the game screen, or `null` while the game screen is not on the stage yet. */
fun Stage.questDetailView(): QuestDetailView? = descendants().filterIsInstance<QuestDetailView>().firstOrNull()

fun Stage.talkView(): TalkView? = descendants().filterIsInstance<TalkView>().firstOrNull()

fun Stage.questJournalView(): QuestJournalView? = descendants().filterIsInstance<QuestJournalView>().firstOrNull()

/** The card announcing the main quest of the game screen, or `null` while the game screen is not on the stage yet. */
fun Stage.questAnnouncementView(): QuestAnnouncementView? = descendants().filterIsInstance<QuestAnnouncementView>().firstOrNull()

/**
 * Whether the start of the current turn has been announced: the banner displays the turn and the battlefield is no
 * longer tinted. The player can not act on the game screen until then.
 */
fun Stage.isTurnAnnounced(): Boolean {
    val bannerView = bannerView() ?: return false
    val battlefieldView = battlefieldView() ?: return false
    return !bannerView.playerTurnText().isNullOrEmpty() &&
        bannerView.playerTurnScale() == 1.0 &&
        battlefieldView.tintColor() == null
}

/**
 * Whether the human player can act on the game screen: their turn has been announced and the scene is not replaying
 * the battle, as shown by the finish turn being enabled. End-to-end tests should wait for it before clicking.
 */
fun Stage.canHumanPlayerAct(): Boolean = isTurnAnnounced() && callToActionView()?.isFinishPlayerTurnEnabled() == true

/** Whether the HUD and the call to action are on the screen, which they are only while battling. */
fun Stage.isHudShown(): Boolean = hudView()?.isShown() == true && callToActionView()?.isShown() == true
