package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.building.adapters.presentation.BuildingApi
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.inventory.adapters.presentation.InventoryApi
import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.lootDrop.adapters.presentation.LootDropApi
import com.mkz.rpg.lootTable.adapters.presentation.LootTableApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addUpdater
import korlibs.korge.view.container
import korlibs.math.geom.Size
import kotlin.random.Random

// Localization: the texts of the selected language. Random: the rolls of the CPU battle brain, e.g.: whether a CPU
// battle unit rests while wandering. The tests choose them.
class BattleScene(
    private val localizationApi: LocalizationApi,
    private val random: Random = Random.Default,
) : Scene() {
    // The scene coroutines (e.g.: the presentation timeline) run on the game window event loop, like the frames, so
    // the views are only changed from that loop. KorGE adds it to the views coroutine context when running the game,
    // but not when running the headless tests (ViewsForTesting), which would run them on Dispatchers.Default instead.
    override val coroutineContext by lazy { super.coroutineContext + views.gameWindow.coroutineDispatcher }

    private val texts = Texts(localizationApi)
    private val resourcesLoader = BattleSceneResourcesLoader()
    val battlefieldView = BattlefieldView(resourcesLoader)
    val battleUnitInfoView = BattleUnitInfoView(resourcesLoader, texts)
    val attackPreviewView = AttackPreviewView(resourcesLoader, texts)
    val inventoryView = InventoryView(resourcesLoader, texts)
    val battleHudView =
        BattleHudView(
            Size(390, 300),
            battleUnitInfoView,
            attackPreviewView,
            inventoryView,
        )
    val playerCallToActionView = PlayerCallToActionView(texts, resourcesLoader)
    val virtualPadView = VirtualPadView(Size(390, 300))
    val npcDialogView = NpcDialogView(Size(390, 300), resourcesLoader)
    val shopView = ShopView(resourcesLoader, texts)
    val formationView = FormationView(resourcesLoader, texts)
    val pauseView = PauseView(texts)
    val confirmDialogView = ConfirmDialogView()

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
        val terrainApi = TerrainApi(eventBus)
        val unitApi = UnitApi(eventBus)
        val playerApi = PlayerApi(eventBus)
        val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
        val buildingApi = BuildingApi(battlefieldApi, eventBus)
        val effectApi = EffectApi(eventBus)
        val abilityApi = AbilityApi(effectApi, eventBus)
        val lootableItemApi = LootableItemApi(effectApi, eventBus)
        val lootTableApi = LootTableApi(lootableItemApi, eventBus)
        val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
        val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus)
        val battleApi = BattleApi(eventBus, playerApi)
        val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, random)
        val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
        val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)
        val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus)
        val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
        val battleSetupApi = BattleSetupApi(eventBus)

        // Resources: each subdomain loads its data before the battle is set up
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

        // Main scene: the presenters replay the battle events in order on the timeline
        val timeline = BattleSceneTimeline(this@BattleScene)
        val battlefieldPresenter =
            BattlefieldPresenter(
                battlefieldView,
                battleUnitInfoView,
                attackPreviewView,
                playerCallToActionView,
                battleHudView,
                battlefieldApi,
                battleUnitApi,
                playerApi,
                abilityApi,
                battleApi,
                partyApi,
                timeline,
                eventBus,
            )
        val inventoryPresenter =
            InventoryPresenter(
                inventoryView,
                battleUnitInfoView,
                battleHudView,
                playerCallToActionView,
                inventoryApi,
                lootableItemApi,
                battleUnitApi,
                playerApi,
                battleApi,
                partyApi,
                battlefieldApi,
                battlefieldView,
                texts,
                timeline,
                eventBus,
            )
        battlefieldPresenter.setTileInterceptor(inventoryPresenter::tileSelected)
        inventoryPresenter.setBattleUnitSelector(battlefieldPresenter::select)

        uiVerticalStack(padding = 2.0) {
            uiSpacing(Size(0, 10))
            // Battle info
            val battleInfoView = BattleInfoView(texts)
            addChild(battleInfoView)
            val battleInfoPresenter =
                BattleInfoPresenter(
                    battleInfoView,
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
                // While exploring, the virtual pad takes the place of the battle HUD, an npc dialog the place of the pad, and a
                // merchant's shop the place of its dialog. The party formation and the pause screen also take the place of the pad.
                container {
                    addChild(battleHudView)
                    addChild(virtualPadView)
                    addChild(npcDialogView)
                    addChild(shopView)
                    addChild(formationView)
                    addChild(pauseView)
                    addChild(confirmDialogView)
                }
                uiSpacing(Size(0, 5))
                // val finishTurnView = FinishTurnView()
                // addChild(finishTurnView)
                // val finishTurnPresenter = FinishTurnPresenter(finishTurnView, battleApi, eventBus)
                addChild(playerCallToActionView)
                FinishTurnPresenter(playerCallToActionView, battleApi, playerApi, timeline, eventBus)
                val shopPresenter =
                    ShopPresenter(
                        shopView,
                        battlefieldView,
                        partyApi,
                        inventoryApi,
                        lootableItemApi,
                        battleUnitApi,
                        battlefieldApi,
                        texts,
                        timeline,
                        eventBus,
                    )
                val formationPresenter = FormationPresenter(formationView, battlefieldView, partyApi, battleUnitApi, battlefieldApi, texts, timeline)
                val pausePresenter =
                    PausePresenter(pauseView, confirmDialogView, partyApi, texts, timeline) {
                        launchImmediately { sceneContainer.changeTo { MenuScene(localizationApi, random) } }
                    }
                val explorationPresenter =
                    ExplorationPresenter(
                        virtualPadView,
                        npcDialogView,
                        battleHudView,
                        playerCallToActionView,
                        partyApi,
                        npcApi,
                        shopPresenter,
                        formationPresenter,
                        pausePresenter,
                        texts,
                        timeline,
                        eventBus,
                    )
                addUpdater { elapsed -> explorationPresenter.tick(elapsed) }
            }
        }

        // Start game
        battleSetupApi.setupBattle()
    }
}
