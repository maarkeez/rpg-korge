package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

// Page object of [BattleScene], used by the end-to-end tests to find the scene views on the stage.

/** The battlefield on the screen, or `null` while the battle scene is not on the stage yet. */
fun Stage.battlefieldView(): BattlefieldView? = descendants().filterIsInstance<BattlefieldView>().firstOrNull()
