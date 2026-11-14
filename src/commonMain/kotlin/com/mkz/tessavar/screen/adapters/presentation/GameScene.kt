package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.building.adapters.presentation.BuildingApi
import com.mkz.tessavar.building.adapters.resources.ResourcesBuildingLoader
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.inventory.adapters.presentation.InventoryApi
import com.mkz.tessavar.localization.adapters.presentation.LocalizationApi
import com.mkz.tessavar.lootDrop.adapters.presentation.LootDropApi
import com.mkz.tessavar.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.quest.adapters.presentation.QuestApi
import com.mkz.tessavar.quest.adapters.resources.ResourcesQuestLoader
import com.mkz.tessavar.save.adapters.presentation.SaveApi
import com.mkz.tessavar.save.domain.SaveError
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.telemetry.adapters.presentation.TelemetryApi
import com.mkz.tessavar.telemetry.domain.TelemetryEvent
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
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
    private val telemetryApi: TelemetryApi,
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
        telemetryApi.listen(eventBus)

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
                        telemetryApi.snapshottable,
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
                    PausePresenter(pauseView, saveSlotsView, confirmDialogView, callToActionView, partyApi, saveApi, messages, timeline, eventBus) {
                        launchImmediately { sceneContainer.changeTo { MenuScene(localizationApi, telemetryApi, storage, random) } }
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
                eventBus.publish(TelemetryEvent.RequestRecordGameLoaded(slot))
            } catch (_: SaveError) {
                launchImmediately { sceneContainer.changeTo { MenuScene(localizationApi, telemetryApi, storage, random, incompatibleSlot = slot) } }
            }
        }
    }
}
