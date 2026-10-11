package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

// Page object of [BattleScene], used by the end-to-end tests to find the scene views on the stage.

/** The battlefield on the screen, or `null` while the battle scene is not on the stage yet. */
fun Stage.battlefieldView(): BattlefieldView? = descendants().filterIsInstance<BattlefieldView>().firstOrNull()

/** The player call to action on the screen, or `null` while the battle scene is not on the stage yet. */
fun Stage.playerCallToActionView(): PlayerCallToActionView? = descendants().filterIsInstance<PlayerCallToActionView>().firstOrNull()

/** The battle HUD on the screen, or `null` while the battle scene is not on the stage yet. */
fun Stage.battleHudView(): BattleHudView? = descendants().filterIsInstance<BattleHudView>().firstOrNull()

/** The selected battle unit info on the screen, or `null` while the battle scene is not on the stage yet. */
fun Stage.battleUnitInfoView(): BattleUnitInfoView? = descendants().filterIsInstance<BattleUnitInfoView>().firstOrNull()

/** The battle info on the screen, or `null` while the battle scene is not on the stage yet. */
fun Stage.battleInfoView(): BattleInfoView? = descendants().filterIsInstance<BattleInfoView>().firstOrNull()

/**
 * Whether the start of the current turn has been announced: the header displays the turn and the battlefield is no
 * longer tinted. The player can not act on the battle scene until then.
 */
fun Stage.isTurnAnnounced(): Boolean {
    val battleInfoView = battleInfoView() ?: return false
    val battlefieldView = battlefieldView() ?: return false
    return !battleInfoView.playerTurnText().isNullOrEmpty() &&
        battleInfoView.playerTurnScale() == 1.0 &&
        battlefieldView.tintColor() == null
}

/**
 * Whether the human player can act on the battle scene: their turn has been announced and the scene is not replaying
 * the battle, as shown by the finish turn being enabled. End-to-end tests should wait for it before clicking.
 */
fun Stage.canHumanPlayerAct(): Boolean = isTurnAnnounced() && playerCallToActionView()?.isFinishTurnEnabled() == true
