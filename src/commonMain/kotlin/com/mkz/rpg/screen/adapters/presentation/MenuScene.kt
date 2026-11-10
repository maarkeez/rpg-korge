package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.GAME_WINDOW_SIZE
import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.IStorage
import korlibs.korge.service.storage.storage
import korlibs.korge.view.SContainer
import korlibs.time.DateTime
import kotlin.random.Random

class MenuScene(
    private val localizationApi: LocalizationApi,
    private val storage: IStorage? = null,
    private val random: Random = Random.Default,
    private val incompatibleSlot: Int? = null,
) : Scene() {
    private companion object {
        const val PANEL_HEIGHT = 300.0

        val ROW_Y = (GAME_WINDOW_SIZE.height - PANEL_HEIGHT) / 2 + PANEL_HEIGHT + 15
    }

    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    override suspend fun SContainer.sceneMain() {
        val messages = Messages(localizationApi)
        val saveApi =
            SaveApi(
                storage = storage ?: views.storage,
                eventBus = InMemoryEventBus(),
                snapshottables = emptyList(),
                canSave = { false },
                leaderUnitId = { null },
                partySize = { 0 },
                now = { DateTime.nowUnixMillis() },
            )
        lateinit var loadGamePresenter: LoadGamePresenter
        val menuView =
            MenuView(
                messages = messages,
                onNewGameSelected = { launchImmediately { sceneContainer.changeTo { GameScene(localizationApi, storage, random) } } },
                onLoadGameSelected = { loadGamePresenter.open() },
                onWorldBuilderSelected = { launchImmediately { sceneContainer.changeTo { WorldBuilderScene(localizationApi, storage) } } },
            )
        val saveSlotsView = SaveSlotsView(messages)
        val confirmDialogView = ConfirmDialogView()
        menuView.x = (GAME_WINDOW_SIZE.width - menuView.width) / 2
        menuView.y = (GAME_WINDOW_SIZE.height - menuView.height) / 2
        val callToActionView = CallToActionView(messages)
        callToActionView.y = ROW_Y
        listOf(menuView, saveSlotsView, confirmDialogView, callToActionView).forEach(::addChild)
        listOf(saveSlotsView, confirmDialogView).forEach { view -> view.y = (GAME_WINDOW_SIZE.height - PANEL_HEIGHT) / 2 }
        loadGamePresenter =
            LoadGamePresenter(menuView, saveSlotsView, confirmDialogView, callToActionView, saveApi, messages, incompatibleSlot) { slot ->
                launchImmediately { sceneContainer.changeTo { GameScene(localizationApi, storage, random, slot) } }
            }
    }
}
