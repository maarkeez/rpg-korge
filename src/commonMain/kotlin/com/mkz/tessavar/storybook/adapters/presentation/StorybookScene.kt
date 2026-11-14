package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.STORYBOOK_WINDOW_SIZE
import com.mkz.tessavar.localization.adapters.presentation.LocalizationApi
import com.mkz.tessavar.screen.adapters.presentation.GameSceneResourcesLoader
import com.mkz.tessavar.screen.adapters.presentation.Messages
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.storybook.adapters.page.StorybookPage
import com.mkz.tessavar.storybook.usecases.services.StoryLocation
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.position
import korlibs.math.geom.Rectangle

class StorybookScene(
    localizationApi: LocalizationApi,
    private val storyLocation: StoryLocation,
    private val storybookPage: StorybookPage,
) : Scene() {
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    private val messages = Messages(localizationApi)
    private val resourcesLoader = GameSceneResourcesLoader()
    val sidebarView = SidebarView()
    val phoneFrameView = PhoneFrameView()
    val storyInfoView = StoryInfoView()
    val toolbarView = ToolbarView()
    val controlsView = ControlsView()
    val actionsView = ActionsView()
    val zoomView = ZoomView(STORYBOOK_WINDOW_SIZE)

    override suspend fun SContainer.sceneInit() {
        resourcesLoader.load()
    }

    override suspend fun SContainer.sceneMain() {
        val eventBus = InMemoryEventBus()
        addUpdater { eventBus.dispatch() }
        val storybookApi = StorybookApi(eventBus, storyLocation, StorybookApi.STORY_BINDINGS)

        val margin = (STORYBOOK_WINDOW_SIZE.height - PhoneFrameView.SIZE.height) / 2
        addChild(zoomView)
        val storybook = zoomView.storybook
        storybook.addChild(sidebarView)
        storybook.addChild(phoneFrameView.position(SidebarView.WIDTH + margin * 2, margin))
        zoomView.phoneArea = Rectangle(phoneFrameView.pos, PhoneFrameView.SIZE)
        zoomView.dragOwners = listOf(sidebarView, phoneFrameView.screen)
        val panelsX = SidebarView.WIDTH + margin * 4 + PhoneFrameView.SIZE.width
        storybook.addChild(toolbarView.position(panelsX, margin * 2))
        storybook.addChild(storyInfoView.position(panelsX, toolbarView.y + ToolbarView.HEIGHT + margin * 2))
        storybook.addChild(controlsView.position(panelsX, storyInfoView.y + StoryInfoView.HEIGHT + margin))
        storybook.addChild(actionsView.position(panelsX, controlsView.y + ControlsView.MAXIMUM_HEIGHT + margin))

        val storyContext = StoryContext(resourcesLoader, messages)
        StorybookPresenter(sidebarView, toolbarView, storyInfoView, controlsView, actionsView, phoneFrameView, storybookApi, storyContext, storybookPage, eventBus)
        storybookApi.init(storyContext)
    }
}
