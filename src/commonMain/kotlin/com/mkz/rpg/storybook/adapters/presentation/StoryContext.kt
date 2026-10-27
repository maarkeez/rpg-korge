package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.Texts

/** What the stories build their views with, as the game does: its images, loaded once, and its texts. */
class StoryContext(
    val resourcesLoader: BattleSceneResourcesLoader,
    val texts: Texts,
)
