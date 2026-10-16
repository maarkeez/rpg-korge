package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

// Page object of [MapBuilderScene], used by the end-to-end tests to find the scene views on the stage.

/** The map builder header on the screen, or `null` while the map builder scene is not on the stage. */
fun Stage.mapBuilderInfoView(): MapBuilderInfoView? = descendants().filterIsInstance<MapBuilderInfoView>().firstOrNull()

/** The map builder controls on the screen, or `null` while the map builder scene is not on the stage. */
fun Stage.mapBuilderHudView(): MapBuilderHudView? = descendants().filterIsInstance<MapBuilderHudView>().firstOrNull()

/** The map builder actions on the screen, or `null` while the map builder scene is not on the stage. */
fun Stage.mapBuilderCallToActionView(): MapBuilderCallToActionView? = descendants().filterIsInstance<MapBuilderCallToActionView>().firstOrNull()
