package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.building.adapters.presentation.BuildingApi
import com.mkz.rpg.building.adapters.resources.ResourcesBuildingLoader
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.quest.adapters.presentation.QuestApi
import com.mkz.rpg.quest.adapters.resources.ResourcesQuestLoader
import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.SaveError
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.IStorage
import korlibs.korge.service.storage.storage
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.fixedSizeContainer
import korlibs.korge.view.xy
import korlibs.math.geom.Size
import korlibs.time.DateTime
import kotlin.random.Random

// Localization: the messages of the selected language. Storage: where the games are saved, `views.storage` by default.
// Random: the rolls of the CPU battle brain, e.g.: whether a CPU battle unit rests while wandering. The tests choose them.
// Slot: the save slot to load the game from, or `null` to play a new game.
class GameScene(
    private val localizationApi: LocalizationApi,
    private val storage: IStorage? = null,
    private val random: Random = Random.Default,
    private val slot: Int? = null,
) : Scene() {
    private companion object {
        // The detail of the main quest is centred in the slot of the pad, as it is narrower.
        const val QUEST_DETAIL_X = 6.0
    }

    // The scene coroutines (e.g.: the presentation timeline) run on the game window event loop, like the frames, so
    // the views are only changed from that loop. KorGE adds it to the views coroutine context when running the game,
    // but not when running the headless tests (ViewsForTesting), which would run them on Dispatchers.Default instead.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    private val messages = Messages(localizationApi)
    private val resourcesLoader = GameSceneResourcesLoader()
    val battlefieldView = BattlefieldView(resourcesLoader)
    val battleUnitInfoView = BattleUnitInfoView(resourcesLoader, messages)
    val castPreviewView = CastPreviewView(resourcesLoader, messages)
    val inventoryView = InventoryView(resourcesLoader, messages)
    val giveGoldView = GiveGoldView(resourcesLoader, messages)
    val hudView =
        HudView(
            Size(390, 300),
            battleUnitInfoView,
            castPreviewView,
            inventoryView,
            giveGoldView,
        )
    val callToActionView = CallToActionView(messages, resourcesLoader)
    val virtualPadView = VirtualPadView(VirtualPadView.SIZE)
    val talkView = TalkView(Size(390, 300), resourcesLoader)
    val shopView = ShopView(resourcesLoader, messages)
    val formationView = FormationView(resourcesLoader)
    val pauseView = PauseView(messages)
    val saveSlotsView = SaveSlotsView(messages)
    val confirmDialogView = ConfirmDialogView()
    val questTrackerView = QuestTrackerView(messages)
    val questDetailView = QuestDetailView(resourcesLoader, messages)
    val questJournalView = QuestJournalView(resourcesLoader, messages)
    val questAnnouncementView = QuestAnnouncementView(resourcesLoader, messages)

    override suspend fun SContainer.sceneInit() {
        resourcesLoader.load()
    }

    override suspend fun SContainer.sceneMain() {
        // Event bus
        val eventBus = InMemoryEventBus()
        addUpdater {
            eventBus.dispatch()
        }

        // Backend APIs
        val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
        val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
        val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
        val buildingApi = BuildingApi(battlefieldApi, eventBus, ResourcesBuildingLoader())
        val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
        val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
        val lootableItemApi = LootableItemApi(effectApi, eventBus, ResourcesLootableItemLoader())
        val lootTableApi = LootTableApi(lootableItemApi, eventBus, ResourcesLootTableLoader())
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
        val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus, ResourcesLootDropLoader(), Random.Default)
        val battleApi = BattleApi(eventBus, playerApi)
        val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, random)
        val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
        val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)
        val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, ResourcesNpcLoader())
        val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
        val questApi = QuestApi(battlefieldApi, partyApi, eventBus, ResourcesQuestLoader())
        val gameSetupApi = GameSetupApi(eventBus)
        // The subdomains holding runtime state, in the order they are initialised, so they are restored in that order.
        val saveApi =
            SaveApi(
                storage = storage ?: views.storage,
                eventBus = eventBus,
                snapshottables =
                    listOf(
                        battleUnitApi.snapshottable,
                        battlefieldApi.snapshottable,
                        npcApi.snapshottable,
                        lootDropApi.snapshottable,
                        inventoryApi.snapshottable,
                        partyApi.snapshottable,
                        cpuWanderBrainApi.snapshottable,
                        questApi.snapshottable,
                    ),
                canSave = { partyApi.searchParty()?.isPaused == true },
                leaderUnitId = {
                    partyApi
                        .searchParty()
                        ?.leaderBattleUnitId
                        ?.let(battleUnitApi.searchBattleUnitById::invoke)
                        ?.unitId
                },
                partySize = { partyApi.searchParty()?.battleUnitIds?.size ?: 0 },
                now = { DateTime.nowUnixMillis() },
            )

        // Resources: each subdomain loads its data before the game is set up
        terrainApi.init()
        effectApi.init()
        lootableItemApi.init()
        lootTableApi.init()
        abilityApi.init()
        unitApi.init()
        lootDropApi.init()
        playerApi.init()
        battlefieldApi.init()
        buildingApi.init()
        battleUnitApi.init()
        npcApi.init()
        questApi.init()

        // Main scene: the presenters replay the battle events in order on the timeline
        val timeline = GameSceneTimeline(this@GameScene)
        val battlefieldPresenter =
            BattlefieldPresenter(
                battlefieldView,
                battleUnitInfoView,
                castPreviewView,
                callToActionView,
                hudView,
                battlefieldApi,
                battleUnitApi,
                playerApi,
                abilityApi,
                effectApi,
                unitApi,
                battleApi,
                partyApi,
                npcApi,
                lootDropApi,
                timeline,
                eventBus,
            )
        val inventoryPresenter =
            InventoryPresenter(
                inventoryView,
                giveGoldView,
                battleUnitInfoView,
                hudView,
                callToActionView,
                inventoryApi,
                lootableItemApi,
                battleUnitApi,
                playerApi,
                battleApi,
                partyApi,
                battlefieldApi,
                battlefieldView,
                messages,
                timeline,
                eventBus,
            )
        battlefieldPresenter.setTileInterceptor(inventoryPresenter::tileSelected)
        inventoryPresenter.setBattleUnitSelector(battlefieldPresenter::select)

        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            // Banner, sharing the top line with the tracker of the main quest, so the stack does not move.
            val bannerView = BannerView(messages)
            fixedSizeContainer(Size(390, 33)) {
                addChild(bannerView)
                addChild(questTrackerView)
            }
            val questPresenter =
                QuestPresenter(
                    questTrackerView,
                    questDetailView,
                    questJournalView,
                    confirmDialogView,
                    questAnnouncementView,
                    battlefieldView,
                    callToActionView,
                    questApi,
                    partyApi,
                    npcApi,
                    messages,
                    timeline,
                    eventBus,
                )
            val bannerPresenter =
                BannerPresenter(
                    bannerView,
                    battlefieldView,
                    battleApi,
                    playerApi,
                    timeline,
                    eventBus,
                )
            // Battlefield
            addChild(battlefieldView)

            uiVerticalStack(padding = 5.0) {
                uiSpacing(Size(0, 10))
                // While exploring, the virtual pad takes the place of the HUD, the talk panel the place of the pad, and a
                // merchant's shop the place of its talk panel. The party formation, the pause screen and the detail of the main quest
                // also take the place of the pad.
                // It is as tall as the HUD: the taller pad reaches below it, over the empty call to action, without
                // pushing the call to action down.
                fixedSizeContainer(Size(390, 300)) {
                    addChild(hudView)
                    addChild(virtualPadView)
                    addChild(talkView)
                    addChild(shopView)
                    addChild(formationView)
                    addChild(pauseView)
                    addChild(saveSlotsView)
                    addChild(confirmDialogView)
                    addChild(questDetailView)
                    questDetailView.xy(QUEST_DETAIL_X, 0.0)
                    addChild(questJournalView)
                    questJournalView.xy(QUEST_DETAIL_X, 0.0)
                }
                uiSpacing(Size(0, 5))
                // val finishPlayerTurnView = FinishPlayerTurnView()
                // addChild(finishPlayerTurnView)
                // val finishPlayerTurnPresenter = FinishPlayerTurnPresenter(finishPlayerTurnView, battleApi, eventBus)
                addChild(callToActionView)
                FinishPlayerTurnPresenter(callToActionView, battleApi, playerApi, timeline, eventBus)
                val shopPresenter =
                    ShopPresenter(
                        shopView,
                        battlefieldView,
                        callToActionView,
                        partyApi,
                        inventoryApi,
                        lootableItemApi,
                        battleUnitApi,
                        battlefieldApi,
                        messages,
                        timeline,
                        eventBus,
                    )
                val formationPresenter = FormationPresenter(formationView, battlefieldView, callToActionView, partyApi, battleUnitApi, battlefieldApi, messages, timeline)
                val pausePresenter =
                    PausePresenter(pauseView, saveSlotsView, confirmDialogView, callToActionView, partyApi, saveApi, messages, timeline) {
                        launchImmediately { sceneContainer.changeTo { MenuScene(localizationApi, storage, random) } }
                    }
                val explorationPresenter =
                    ExplorationPresenter(
                        virtualPadView,
                        talkView,
                        hudView,
                        callToActionView,
                        partyApi,
                        npcApi,
                        shopPresenter,
                        formationPresenter,
                        pausePresenter,
                        questPresenter,
                        messages,
                        timeline,
                        eventBus,
                    )
                addUpdater { elapsed -> explorationPresenter.tick(elapsed) }
            }
        }

        // Over everything, taking no room in the stack.
        addChild(questAnnouncementView)

        // Start game: a new one, or the one saved in the slot, restored once the content read from the resources is set
        // up. A save that can not be loaded goes back to Load game, where it is shown as incompatible.
        if (slot == null) {
            gameSetupApi.setUpGame()
        } else {
            gameSetupApi.setUpGame(isLoadingASave = true)
            eventBus.dispatch()
            try {
                saveApi.loadGame(slot)
            } catch (_: SaveError) {
                launchImmediately { sceneContainer.changeTo { MenuScene(localizationApi, storage, random, incompatibleSlot = slot) } }
            }
        }
    }
}
