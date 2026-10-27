package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

// Page object of [StorybookScene].

/** The sidebar on the screen, or `null` while the Storybook is not on the stage. */
fun Stage.sidebarView(): SidebarView? = descendants().filterIsInstance<SidebarView>().firstOrNull()

/** The phone on the screen, or `null` while the Storybook is not on the stage. */
fun Stage.phoneFrameView(): PhoneFrameView? = descendants().filterIsInstance<PhoneFrameView>().firstOrNull()

/** The story info on the screen, or `null` while the Storybook is not on the stage. */
fun Stage.storyInfoView(): StoryInfoView? = descendants().filterIsInstance<StoryInfoView>().firstOrNull()
