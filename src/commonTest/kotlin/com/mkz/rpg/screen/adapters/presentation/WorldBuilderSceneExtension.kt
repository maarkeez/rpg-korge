package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

fun Stage.worldBuilderInfoView(): WorldBuilderInfoView? = descendants().filterIsInstance<WorldBuilderInfoView>().firstOrNull()

fun Stage.worldBuilderHudView(): WorldBuilderHudView? = descendants().filterIsInstance<WorldBuilderHudView>().firstOrNull()

fun Stage.worldBuilderCallToActionView(): WorldBuilderCallToActionView? = descendants().filterIsInstance<WorldBuilderCallToActionView>().firstOrNull()
