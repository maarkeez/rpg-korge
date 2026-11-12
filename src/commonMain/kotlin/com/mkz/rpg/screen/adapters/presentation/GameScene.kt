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

class GameScene(
    private val localizationApi: LocalizationApi,
    private val storage: IStorage? = null,
    private val random: Random = Random.Default,
    private val slot: Int? = null,
) : Scene() {
    private companion object {
        const val QUEST_DETAIL_X = 6.0
    }

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
        val eventBus = InMemoryEventBus()
        addUpdater {
            eventBus.dispatch()
        }

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
        val battleApi = BattleApi(eventBus)
        val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, random)
        val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
        val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)
        val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, ResourcesNpcLoader())
        val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
        val questApi = QuestApi(battlefieldApi, partyApi, eventBus, ResourcesQuestLoader())
        val gameSetupApi = GameSetupApi(eventBus)
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
            addChild(battlefieldView)

            uiVerticalStack(padding = 5.0) {
                uiSpacing(Size(0, 10))
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
                        questApi,
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

        addChild(questAnnouncementView)

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
