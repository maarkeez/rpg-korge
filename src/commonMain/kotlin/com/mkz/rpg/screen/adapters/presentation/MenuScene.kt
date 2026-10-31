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

/**
 * The first scene of the game, opening the battle, a saved game or the map builder. Localization: the texts of the
 * selected language, kept by every scene. Storage: where the games are saved, `views.storage` by default, also kept by
 * every scene. Random: the rolls of the CPU battle brain in the battle. Incompatible slot: the slot whose game could not
 * be loaded, if any, so Load game lists the slots straight away, that one as incompatible.
 */
class MenuScene(
    private val localizationApi: LocalizationApi,
    private val storage: IStorage? = null,
    private val random: Random = Random.Default,
    private val incompatibleSlot: Int? = null,
) : Scene() {
    private companion object {
        // The height of the save slots and of the question, as in place of the pad.
        const val PANEL_HEIGHT = 300.0
    }

    // Like the battle scene, the scene coroutines run on the game window event loop, also in the headless tests.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    override suspend fun SContainer.sceneMain() {
        val texts = Texts(localizationApi)
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
                texts = texts,
                onNewGameSelected = { launchImmediately { sceneContainer.changeTo { BattleScene(localizationApi, storage, random) } } },
                onLoadGameSelected = { loadGamePresenter.open() },
                onMapBuilderSelected = { launchImmediately { sceneContainer.changeTo { MapBuilderScene(localizationApi, storage) } } },
            )
        // The save slots, and the question before deleting one, take the place of the options: as wide as the window.
        val saveSlotsView = SaveSlotsView(texts)
        val confirmDialogView = ConfirmDialogView()
        menuView.x = (GAME_WINDOW_SIZE.width - menuView.width) / 2
        menuView.y = (GAME_WINDOW_SIZE.height - menuView.height) / 2
        listOf(menuView, saveSlotsView, confirmDialogView).forEach(::addChild)
        listOf(saveSlotsView, confirmDialogView).forEach { view -> view.y = (GAME_WINDOW_SIZE.height - PANEL_HEIGHT) / 2 }
        loadGamePresenter =
            LoadGamePresenter(menuView, saveSlotsView, confirmDialogView, saveApi, texts, incompatibleSlot) { slot ->
                launchImmediately { sceneContainer.changeTo { BattleScene(localizationApi, storage, random, slot) } }
            }
    }
}
