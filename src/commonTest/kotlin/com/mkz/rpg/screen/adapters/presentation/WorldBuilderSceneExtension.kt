package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

// Page object of [WorldBuilderScene], used by the end-to-end tests to find the scene views on the stage.

/** The world builder header on the screen, or `null` while the world builder scene is not on the stage. */
fun Stage.worldBuilderInfoView(): WorldBuilderInfoView? = descendants().filterIsInstance<WorldBuilderInfoView>().firstOrNull()

/** The world builder controls on the screen, or `null` while the world builder scene is not on the stage. */
fun Stage.worldBuilderHudView(): WorldBuilderHudView? = descendants().filterIsInstance<WorldBuilderHudView>().firstOrNull()

/** The world builder actions on the screen, or `null` while the world builder scene is not on the stage. */
fun Stage.worldBuilderCallToActionView(): WorldBuilderCallToActionView? = descendants().filterIsInstance<WorldBuilderCallToActionView>().firstOrNull()
