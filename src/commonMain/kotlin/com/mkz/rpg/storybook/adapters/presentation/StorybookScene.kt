package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.STORYBOOK_WINDOW_SIZE
import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.Texts
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.storybook.usecases.services.StoryLocation
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.position

/**
 * The Storybook: the catalogue in a sidebar on the left, the story on show in a phone in the middle, and what it is on
 * the right. Localization: the texts of the views on show. Location: where the story on show is kept, e.g.: the page URL.
 */
class StorybookScene(
    localizationApi: LocalizationApi,
    private val storyLocation: StoryLocation,
) : Scene() {
    // Like the battle scene, the scene coroutines run on the game window event loop, also in the headless tests.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    private val texts = Texts(localizationApi)
    private val resourcesLoader = BattleSceneResourcesLoader()
    val sidebarView = SidebarView()
    val phoneFrameView = PhoneFrameView()
    val storyInfoView = StoryInfoView()

    override suspend fun SContainer.sceneInit() {
        resourcesLoader.load()
    }

    override suspend fun SContainer.sceneMain() {
        val eventBus = InMemoryEventBus()
        addUpdater { eventBus.dispatch() }
        val storybookApi = StorybookApi(eventBus, storyLocation)

        // The phone is as far from the sidebar as from the top, so its frame fits the window height.
        val margin = (STORYBOOK_WINDOW_SIZE.height - PhoneFrameView.SIZE.height) / 2
        addChild(sidebarView)
        addChild(phoneFrameView.position(SidebarView.WIDTH + margin * 2, margin))
        addChild(storyInfoView.position(SidebarView.WIDTH + margin * 4 + PhoneFrameView.SIZE.width, margin * 2))

        StorybookPresenter(sidebarView, storyInfoView, phoneFrameView, storybookApi, StoryContext(resourcesLoader, texts), eventBus)
        storybookApi.init()
    }
}
