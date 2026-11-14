package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.descendants
import korlibs.korge.view.Stage

fun Stage.sidebarView(): SidebarView? = descendants().filterIsInstance<SidebarView>().firstOrNull()

fun Stage.phoneFrameView(): PhoneFrameView? = descendants().filterIsInstance<PhoneFrameView>().firstOrNull()

fun Stage.storyInfoView(): StoryInfoView? = descendants().filterIsInstance<StoryInfoView>().firstOrNull()

fun Stage.toolbarView(): ToolbarView? = descendants().filterIsInstance<ToolbarView>().firstOrNull()

fun Stage.controlsView(): ControlsView? = descendants().filterIsInstance<ControlsView>().firstOrNull()

fun Stage.actionsView(): ActionsView? = descendants().filterIsInstance<ActionsView>().firstOrNull()
