package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.effect.domain.EffectMother.healEffect
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.quest.domain.QuestMother.mainQuest
import com.mkz.rpg.save.adapters.presentation.SaveApi
import com.mkz.rpg.save.domain.SaveEvent
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.VirtualPadView.Direction
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.math.geom.Size
import korlibs.time.DateTime
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ExplorationPresenterTest {
    // The knight explores the sand from (1,1), next to the elder, who stands west of it at (1,0). The merchant stands at
    // (2,2), selling a health potion and a mana potion. There is no battle yet.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            effects = listOf(healEffect(id = "cure").toDto()),
            lootableItems =
                listOf(
                    consumableLootableItem(id = "health-potion", price = 10, consumable = consumable(effectIds = setOf("cure"))).toDto(),
                    consumableLootableItem(id = "mana-potion", price = 15, consumable = consumable(effectIds = setOf("cure"))).toDto(),
                ),
            units = listOf(unit(id = "knight").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(3) { List(3) { "sand" } },
            deployments = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1)),
            npcs =
                listOf(
                    npc(id = "elder", row = 1, column = 0).toDto(),
                    npc(id = "merchant", row = 2, column = 2, stock = listOf("health-potion", "mana-potion")).toDto(),
                ),
            quests = listOf(mainQuest(id = "defeat-the-goat-wizard").toDto()),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val virtualPadView = VirtualPadView()
    private val npcDialogView = NpcDialogView(Size(390, 300), BattleSceneResourcesLoader())
    private val shopView = ShopView(BattleSceneResourcesLoader(), TextsMother.texts())
    private val battlefieldView = FakeBattlefieldView()
    private val battleHudView = FakeBattleHudView()
    private val playerCallToActionView = FakePlayerCallToActionView()
    private val timeline = battleSceneTimeline()
    private val shopPresenter =
        ShopPresenter(
            shopView,
            battlefieldView,
            playerCallToActionView,
            apis.partyApi,
            apis.inventoryApi,
            apis.lootableItemApi,
            apis.battleUnitApi,
            apis.battlefieldApi,
            TextsMother.texts(),
            timeline,
            eventBus,
        )

    private val formationView = FormationView(BattleSceneResourcesLoader())
    private val formationPresenter = FormationPresenter(formationView, battlefieldView, playerCallToActionView, apis.partyApi, apis.battleUnitApi, apis.battlefieldApi, TextsMother.texts(), timeline)
    private val pauseView = PauseView(TextsMother.texts())
    private val saveSlotsView = SaveSlotsView(TextsMother.texts())
    private val confirmDialogView = ConfirmDialogView()
    private val pausePresenter =
        PausePresenter(
            pauseView,
            saveSlotsView,
            confirmDialogView,
            playerCallToActionView,
            apis.partyApi,
            SaveApi(InmemoryStorage(), eventBus, emptyList(), {
                false
            }, { null }, { 0 }, { DateTime.nowUnixMillis() }),
            TextsMother.texts(),
            timeline,
        ) {}

    private val questDetailView = QuestDetailView(BattleSceneResourcesLoader(), TextsMother.texts())
    private val questPresenter =
        QuestPresenter(
            FakeQuestTrackerView(),
            questDetailView,
            QuestJournalView(BattleSceneResourcesLoader(), TextsMother.texts()),
            ConfirmDialogView(),
            FakeQuestAnnouncementView(),
            FakeBattlefieldView(),
            playerCallToActionView,
            apis.questApi,
            apis.partyApi,
            apis.npcApi,
            TextsMother.texts(),
            timeline,
            eventBus,
        )

    private fun createPresenter() =
        ExplorationPresenter(
            virtualPadView,
            npcDialogView,
            battleHudView,
            playerCallToActionView,
            apis.partyApi,
            apis.npcApi,
            shopPresenter,
            formationPresenter,
            pausePresenter,
            questPresenter,
            TextsMother.texts(),
            timeline,
            eventBus,
        )

    private fun startExploring() {
        apis.partyApi.startExploration()
        eventBus.dispatch()
    }

    @Test
    fun `Init should hide the virtual pad when is created and the exploration has not started yet`() {
        // When
        createPresenter()
        // Then
        virtualPadView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayExploring should display the virtual pad in place of the battle HUD and the call to action when the exploration starts`() {
        // Given
        createPresenter()
        // When
        startExploring()
        // Then
        virtualPadView.visible.shouldBeTrue()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayExploring should display the battle HUD and the call to action in place of the virtual pad when the exploration is interrupted`() {
        // Given
        createPresenter()
        startExploring()
        // When
        eventBus.publish(PartyEvent.ExplorationInterrupted(playerId = "human"))
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
    }

    @Test
    fun `BattleStarted should hide the virtual pad before the steps waiting in the timeline`() {
        // Given: the leader is landing on its last tile, and the battle is waiting to be announced.
        createPresenter()
        startExploring()
        val leaderLanding = timeline.playLongStep()
        var isPadVisibleWhenAnnounced: Boolean? = null
        timeline.play { isPadVisibleWhenAnnounced = virtualPadView.visible }
        // When
        eventBus.publish(BattleStarted)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeTrue()
        leaderLanding.complete(Unit)
        isPadVisibleWhenAnnounced shouldBe false
        battleHudView.visible.shouldBeTrue()
    }

    @Test
    fun `DisplayExploring should display the virtual pad in place of the battle HUD and the call to action when the exploration resumes`() {
        // Given
        createPresenter()
        startExploring()
        eventBus.publish(PartyEvent.ExplorationInterrupted(playerId = "human"))
        eventBus.dispatch()
        // When
        eventBus.publish(PartyEvent.ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1")))
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeTrue()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `StepParty should step the party towards the held arrow when the scene is idle`() {
        // Given
        createPresenter()
        startExploring()
        // When
        virtualPadView.holdArrow(Direction.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
    }

    @Test
    fun `StepParty should step the party one tile towards each arrow when it is held`() {
        // Given
        createPresenter()
        startExploring()
        val walks =
            listOf(
                Direction.NORTH to PositionDto(row = 0, column = 1),
                Direction.SOUTH to PositionDto(row = 1, column = 1),
                Direction.EAST to PositionDto(row = 1, column = 2),
                Direction.WEST to PositionDto(row = 1, column = 1),
            )
        walks.forEach { (direction, expectedPosition) ->
            // When
            tapArrow(direction)
            eventBus.dispatch()
            // Then
            apis.battlefieldApi.searchPosition("knight-1") shouldBe expectedPosition
        }
    }

    @Test
    fun `StepParty should step the party once the scene is idle when an arrow was tapped while it played`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        // When
        tapArrow(Direction.NORTH)
        presenter.tick(1.milliseconds)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
        // When
        longStep.complete(Unit)
        presenter.tick(1.milliseconds)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
    }

    @Test
    fun `StepParty should step only towards the last arrow tapped while the scene played`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        tapArrow(Direction.NORTH)
        tapArrow(Direction.EAST)
        // When
        longStep.complete(Unit)
        presenter.tick(1.milliseconds)
        eventBus.dispatch()
        presenter.tick(1.milliseconds)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 2)
    }

    @Test
    fun `StepParty should forget the arrow tapped while the scene played when the pad is hidden`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        tapArrow(Direction.NORTH)
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        longStep.complete(Unit)
        presenter.tick(1.milliseconds)
        // When
        eventBus.publish(BattlefieldHudEvent.Idle)
        eventBus.dispatch()
        presenter.tick(1.milliseconds)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeTrue()
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
    }

    private fun tapArrow(direction: Direction) {
        virtualPadView.holdArrow(direction)
        virtualPadView.releaseArrow()
    }

    private fun requestedTerrainEffects() =
        mutableListOf<Set<String>>().also { requests ->
            eventBus.subscribe<BattlefieldEvent.RequestTerrainEffects> { requests += it.occupantIds }
        }

    @Test
    fun `StepParty should step the party when an arrow is tapped while its three members flash every second on water`() {
        // Given
        val apis = partyOnWaterApis()
        val timeline = battleSceneTimeline()
        val battlefieldView = FakeBattlefieldView().apply { flashing = CompletableDeferred() }
        val presenter = partyOnWaterPresenters(apis, battlefieldView, timeline)
        apis.partyApi.startExploration()
        apis.eventBus.dispatch()
        repeat(3) {
            presenter.tick(1.seconds)
            apis.eventBus.dispatch()
        }
        require(battlefieldView.flashedBattleUnits.size == 9) { "The three members flash every second" }
        // When
        tapArrow(Direction.NORTH)
        presenter.tick(1.milliseconds)
        apis.eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 0)
    }

    // Three knights stand on water, healing them every second, in a column from (1,0), the leader first. Sand is north.
    private fun partyOnWaterApis() =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand", allowedTransitionTo = setOf("water")).toDto(), occupiableTerrain(id = "water", effectId = "water-heal").toDto()),
            effects = listOf(healEffect(id = "water-heal").toDto()),
            units = listOf(unit(id = "knight").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = listOf(listOf("sand"), listOf("water"), listOf("water"), listOf("water")),
            deployments = (1..3).map { index -> Deployment(battleUnitId = "knight-$index", unitId = "knight", playerId = "human", row = index, column = 0) },
        ).also(SubdomainApis::initialise)

    // The battlefield presenter flashes the battle units on the same timeline the exploration presenter walks with.
    private fun partyOnWaterPresenters(
        apis: SubdomainApis,
        battlefieldView: FakeBattlefieldView,
        timeline: BattleSceneTimeline,
    ): ExplorationPresenter {
        BattlefieldPresenter(
            battlefieldView = battlefieldView,
            battleUnitInfoView = FakeBattleUnitInfoView(),
            attackPreviewView = FakeAttackPreviewView(),
            playerCallToActionView = FakePlayerCallToActionView(),
            battleHudView = FakeBattleHudView(),
            battlefieldApi = apis.battlefieldApi,
            battleUnitApi = apis.battleUnitApi,
            playerApi = apis.playerApi,
            abilityApi = apis.abilityApi,
            effectApi = apis.effectApi,
            unitApi = apis.unitApi,
            battleApi = apis.battleApi,
            partyApi = apis.partyApi,
            npcApi = apis.npcApi,
            lootDropApi = apis.lootDropApi,
            timeline = timeline,
            eventBus = apis.eventBus,
        )
        val shopPresenter =
            ShopPresenter(
                shopView,
                battlefieldView,
                playerCallToActionView,
                apis.partyApi,
                apis.inventoryApi,
                apis.lootableItemApi,
                apis.battleUnitApi,
                apis.battlefieldApi,
                TextsMother.texts(),
                timeline,
                apis.eventBus,
            )
        val formationPresenter = FormationPresenter(formationView, battlefieldView, playerCallToActionView, apis.partyApi, apis.battleUnitApi, apis.battlefieldApi, TextsMother.texts(), timeline)
        val pausePresenter =
            PausePresenter(
                pauseView,
                saveSlotsView,
                confirmDialogView,
                playerCallToActionView,
                apis.partyApi,
                SaveApi(InmemoryStorage(), apis.eventBus, emptyList(), {
                    false
                }, { null }, { 0 }, { DateTime.nowUnixMillis() }),
                TextsMother.texts(),
                timeline,
            ) {}
        return ExplorationPresenter(
            virtualPadView,
            npcDialogView,
            battleHudView,
            playerCallToActionView,
            apis.partyApi,
            apis.npcApi,
            shopPresenter,
            formationPresenter,
            pausePresenter,
            QuestPresenter(
                FakeQuestTrackerView(),
                QuestDetailView(BattleSceneResourcesLoader(), TextsMother.texts()),
                QuestJournalView(BattleSceneResourcesLoader(), TextsMother.texts()),
                ConfirmDialogView(),
                FakeQuestAnnouncementView(),
                FakeBattlefieldView(),
                playerCallToActionView,
                apis.questApi,
                apis.partyApi,
                apis.npcApi,
                TextsMother.texts(),
                timeline,
                apis.eventBus,
            ),
            TextsMother.texts(),
            timeline,
            apis.eventBus,
        )
    }

    @Test
    fun `Tick should apply the terrain effects of the battle units standing still every second while exploring`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val requests = requestedTerrainEffects()
        // When
        presenter.tick(999.milliseconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
        // When
        presenter.tick(1.milliseconds)
        presenter.tick(2.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldContainExactly(setOf("knight-1"), setOf("knight-1"), setOf("knight-1"))
    }

    @Test
    fun `Tick should no longer apply the terrain effects and the virtual pad should be hidden when the game is over`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val requests = requestedTerrainEffects()
        eventBus.publish(PlayerVictory(playerId = "cpu"))
        eventBus.dispatch()
        // When
        presenter.tick(1.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
        virtualPadView.visible.shouldBeFalse()
    }

    private val knightSelected = BattlefieldHudEvent.SelectedBattleUnit(tile = TileDto(row = 1, column = 1), battleUnitId = "knight-1", tilesWhereCanBeMoved = emptySet())

    @Test
    fun `DisplayVirtualPad should display the battle HUD and the call to action in place of the pad when a battle unit is selected while exploring`() {
        // Given
        createPresenter()
        startExploring()
        // When
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
    }

    @Test
    fun `DisplayVirtualPad should display the pad again when the battlefield HUD goes idle while exploring`() {
        // Given
        createPresenter()
        startExploring()
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        // When
        eventBus.publish(BattlefieldHudEvent.Idle)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeTrue()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayVirtualPad should keep the battle HUD when the battlefield HUD goes idle while battling`() {
        // Given
        createPresenter()
        startExploring()
        apis.partyApi.interruptExploration()
        eventBus.dispatch()
        // When
        eventBus.publish(BattlefieldHudEvent.Idle)
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
    }

    @Test
    fun `DisplayVirtualPad should display the battle HUD straight away when a found item does not fit in the inventory while exploring`() {
        // Given
        createPresenter()
        startExploring()
        timeline.playLongStep()
        // When
        eventBus.publish(InventoryEvent.InventoryFull(battleUnitId = "knight-1", lootableItemId = "hammer", row = 1, column = 1))
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
    }

    @Test
    fun `StepParty should not step the party while the pad is hidden, even when an arrow is still held`() {
        // Given
        createPresenter()
        startExploring()
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        // When
        virtualPadView.holdArrow(Direction.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
    }

    private fun talk() {
        virtualPadView.pressAButton()
        eventBus.dispatch()
    }

    @Test
    fun `TalkToAdjacentNpc should display the dialog of the npc next to the leader in place of the pad when A is pressed`() {
        // Given
        createPresenter()
        startExploring()
        // When
        talk()
        // Then
        npcDialogView.visible.shouldBeTrue()
        npcDialogView.displayedName() shouldBe "Elder"
        npcDialogView.displayedLine() shouldBe
            "Long ago a goat chewed up a wizard's spellbook and swallowed its magic. The Goat Wizard now rules the lair down south. Beat it, and its bees may answer your call."
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `TalkToAdjacentNpc should talk once the scene is idle when A was pressed while it played`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        // When
        talk()
        // Then
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe null
        // When
        longStep.complete(Unit)
        presenter.tick(1.milliseconds)
        eventBus.dispatch()
        // Then
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "elder"
        npcDialogView.visible.shouldBeTrue()
    }

    @Test
    fun `TalkToAdjacentNpc should do nothing when no npc is next to the leader`() {
        // Given
        createPresenter()
        startExploring()
        virtualPadView.holdArrow(Direction.NORTH)
        virtualPadView.releaseArrow()
        eventBus.dispatch()
        // When
        talk()
        // Then
        npcDialogView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
    }

    @Test
    fun `Tick should not apply the terrain effects while talking`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        talk()
        val requests = requestedTerrainEffects()
        // When
        presenter.tick(3.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
    }

    @Test
    fun `StepParty should not step the party while talking, even when an arrow is still held`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        virtualPadView.holdArrow(Direction.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
    }

    @Test
    fun `FinishTalk should display the pad again and explore when the dialog is tapped`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        npcDialogView.tapDialog()
        eventBus.dispatch()
        // Then
        npcDialogView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
        apis.partyApi
            .searchParty()!!
            .isExploring
            .shouldBeTrue()
    }

    @Test
    fun `DisplayExploring should close the dialog when the exploration is interrupted while talking`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        eventBus.publish(PartyEvent.ExplorationInterrupted(playerId = "human"))
        eventBus.dispatch()
        // Then
        npcDialogView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
    }

    // The leader walks east to (1,2), next to the merchant, south of it, and talks to it.
    private fun talkToTheMerchant() {
        virtualPadView.holdArrow(Direction.EAST)
        virtualPadView.releaseArrow()
        eventBus.dispatch()
        talk()
    }

    // ... and chooses to buy.
    private fun openTheShop() {
        talkToTheMerchant()
        playerCallToActionView.chooseAction("Buy")
        eventBus.dispatch()
    }

    @Test
    fun `ExplorationPresenter should display Leave, Sell and Buy when a merchant talks`() {
        // Given
        createPresenter()
        startExploring()
        // When
        talkToTheMerchant()
        // Then
        playerCallToActionView.actionLabels().shouldContainExactly("Leave", "Sell", "Buy")
    }

    @Test
    fun `ExplorationPresenter should display only Buy in blue when a merchant talks`() {
        // Given
        createPresenter()
        startExploring()
        // When
        talkToTheMerchant()
        // Then
        playerCallToActionView.displayedActions!!.map { action -> action.color }.shouldContainExactly(GameButton.GREY, GameButton.GREY, ConfirmButton.COLOR)
        playerCallToActionView.displayedActions!!.last().pressedColor shouldBe ConfirmButton.PRESSED_COLOR
    }

    @Test
    fun `ExplorationPresenter should display no row when an npc with no choices talks`() {
        // Given
        createPresenter()
        startExploring()
        // When
        talk()
        // Then
        playerCallToActionView.displayedActions shouldBe null
    }

    @Test
    fun `DialogTapped should keep talking to a merchant`() {
        // Given
        createPresenter()
        startExploring()
        talkToTheMerchant()
        // When
        npcDialogView.tapDialog()
        eventBus.dispatch()
        // Then
        npcDialogView.visible.shouldBeTrue()
        shopPresenter.isOpen.shouldBeFalse()
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "merchant"
    }

    @Test
    fun `Buy should open the shop of the merchant to buy in place of its dialog, and keep talking`() {
        // Given
        createPresenter()
        startExploring()
        talkToTheMerchant()
        // When
        playerCallToActionView.chooseAction("Buy")
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeTrue()
        shopView.visible.shouldBeTrue()
        shopView.footerText() shouldBe "Tap an item to buy"
        npcDialogView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeTrue()
        playerCallToActionView.actionLabels().shouldContainExactly("Back")
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "merchant"
    }

    @Test
    fun `Sell should open the shop of the merchant to sell`() {
        // Given
        createPresenter()
        startExploring()
        talkToTheMerchant()
        // When
        playerCallToActionView.chooseAction("Sell")
        eventBus.dispatch()
        // Then
        shopView.visible.shouldBeTrue()
        shopView.footerText() shouldBe "Nothing to sell"
    }

    @Test
    fun `Buy should not open the shop while the scene plays the previous steps`() {
        // Given
        createPresenter()
        startExploring()
        talkToTheMerchant()
        timeline.playLongStep()
        // When
        playerCallToActionView.chooseAction("Buy")
        // Then
        shopPresenter.isOpen.shouldBeFalse()
    }

    @Test
    fun `Leave should finish the talk with the merchant and display the pad again`() {
        // Given
        createPresenter()
        startExploring()
        talkToTheMerchant()
        // When
        playerCallToActionView.chooseAction("Leave")
        eventBus.dispatch()
        // Then
        npcDialogView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
        apis.partyApi
            .searchParty()!!
            .isExploring
            .shouldBeTrue()
    }

    @Test
    fun `ShopBackSelected should close the shop and display the merchant's dialog again, still talking`() {
        // Given
        createPresenter()
        startExploring()
        openTheShop()
        shopPresenter.shopSlotSelected(0)
        // When
        playerCallToActionView.chooseAction("Back")
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeFalse()
        shopView.visible.shouldBeFalse()
        npcDialogView.visible.shouldBeTrue()
        playerCallToActionView.actionLabels().shouldContainExactly("Leave", "Sell", "Buy")
        virtualPadView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeTrue()
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "merchant"
    }

    @Test
    fun `Sell should open the shop to sell after Back from buying`() {
        // Given
        createPresenter()
        startExploring()
        openTheShop()
        playerCallToActionView.chooseAction("Back")
        eventBus.dispatch()
        // When
        playerCallToActionView.chooseAction("Sell")
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeTrue()
        shopView.footerText() shouldBe "Nothing to sell"
        npcDialogView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayExploring should close the shop and display the finish turn again when the exploration is interrupted while shopping`() {
        // Given
        createPresenter()
        startExploring()
        openTheShop()
        shopPresenter.shopSlotSelected(0)
        // When
        eventBus.publish(PartyEvent.ExplorationInterrupted(playerId = "human"))
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
        playerCallToActionView.displayedItemActions shouldBe null
    }

    private fun openFormation() {
        virtualPadView.pressBButton()
        eventBus.dispatch()
    }

    @Test
    fun `OpenFormation should display the party formation in place of the pad when B is pressed`() {
        // Given
        createPresenter()
        startExploring()
        // When
        openFormation()
        // Then
        formationView.visible.shouldBeTrue()
        formationView.names().shouldContainExactly("Knight")
        formationView.crownedRowIndexes().shouldContainExactly(0)
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeTrue()
        playerCallToActionView.actionLabels().shouldContainExactly("Close")
        apis.partyApi
            .searchParty()!!
            .isInFormation
            .shouldBeTrue()
    }

    @Test
    fun `OpenFormation should open once the scene is idle when B was pressed while it played`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        // When
        openFormation()
        // Then
        apis.partyApi
            .searchParty()!!
            .isInFormation
            .shouldBeFalse()
        // When
        longStep.complete(Unit)
        presenter.tick(1.milliseconds)
        eventBus.dispatch()
        // Then
        apis.partyApi
            .searchParty()!!
            .isInFormation
            .shouldBeTrue()
        formationView.visible.shouldBeTrue()
    }

    @Test
    fun `Tick should not apply the terrain effects while in the party formation`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        openFormation()
        val requests = requestedTerrainEffects()
        // When
        presenter.tick(3.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
    }

    @Test
    fun `StepParty should not step the party while in the party formation, even when an arrow is still held`() {
        // Given
        createPresenter()
        startExploring()
        openFormation()
        // When
        virtualPadView.holdArrow(Direction.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 1)
    }

    @Test
    fun `CloseFormation should display the pad again and explore when Close is tapped`() {
        // Given
        createPresenter()
        startExploring()
        openFormation()
        // When
        playerCallToActionView.chooseAction("Close")
        eventBus.dispatch()
        // Then
        formationView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
        apis.partyApi
            .searchParty()!!
            .isExploring
            .shouldBeTrue()
    }

    private fun openPause() {
        virtualPadView.pressStartButton()
        eventBus.dispatch()
    }

    @Test
    fun `OpenPause should display the pause screen in place of the pad when Start is pressed`() {
        // Given
        createPresenter()
        startExploring()
        // When
        openPause()
        // Then
        pauseView.visible.shouldBeTrue()
        virtualPadView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
        apis.partyApi
            .searchParty()!!
            .isPaused
            .shouldBeTrue()
    }

    @Test
    fun `Tick should not apply the terrain effects while paused`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        openPause()
        val requests = requestedTerrainEffects()
        // When
        presenter.tick(3.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
    }

    @Test
    fun `ClosePause should display the pad again and explore when Resume is tapped`() {
        // Given
        createPresenter()
        startExploring()
        openPause()
        // When
        pausePresenter.pauseResumeSelected()
        eventBus.dispatch()
        // Then
        pauseView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
        apis.partyApi
            .searchParty()!!
            .isExploring
            .shouldBeTrue()
    }

    private fun tapTracker() {
        questPresenter.questTrackerSelected()
        eventBus.dispatch()
    }

    private fun isReadingQuest() = apis.partyApi.searchParty()!!.isReadingQuest

    @Test
    fun `ExplorationPresenter should open the quest when the tracker is tapped while the pad is displayed`() {
        // Given
        createPresenter()
        startExploring()
        // When
        tapTracker()
        // Then
        isReadingQuest().shouldBeTrue()
        questPresenter.isQuestOpen.shouldBeTrue()
        questDetailView.visible.shouldBeTrue()
        virtualPadView.visible.shouldBeFalse()
    }

    @Test
    fun `ExplorationPresenter should not open the quest when the tracker is tapped while the timeline is playing, or the pad is hidden`() {
        // Given
        createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        // When
        tapTracker()
        val isReadingWhilePlaying = isReadingQuest()
        longStep.complete(Unit)
        virtualPadView.visible = false
        tapTracker()
        // Then
        isReadingWhilePlaying.shouldBeFalse()
        isReadingQuest().shouldBeFalse()
        questDetailView.visible.shouldBeFalse()
    }

    @Test
    fun `ExplorationPresenter should display the pad when the quest is closed`() {
        // Given
        createPresenter()
        startExploring()
        tapTracker()
        // When
        playerCallToActionView.chooseAction("Back")
        eventBus.dispatch()
        // Then
        isReadingQuest().shouldBeFalse()
        questDetailView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeTrue()
        playerCallToActionView.displayedActions shouldBe null
        playerCallToActionView.visible.shouldBeFalse()
    }

    @Test
    fun `ExplorationPresenter should display the row while the quest is read`() {
        // Given
        createPresenter()
        startExploring()
        // When
        tapTracker()
        // Then
        playerCallToActionView.actionLabels().shouldContainExactly("Back")
        playerCallToActionView.visible.shouldBeTrue()
    }

    @Test
    fun `ExplorationPresenter should hide the row when a battle starts with a panel open`() {
        // Given
        createPresenter()
        startExploring()
        tapTracker()
        // When
        eventBus.publish(PartyEvent.ExplorationInterrupted(playerId = "human"))
        eventBus.dispatch()
        // Then
        questDetailView.visible.shouldBeFalse()
        playerCallToActionView.displayedActions shouldBe null
    }

    @Test
    fun `ExplorationPresenter should hide the row when the player wins`() {
        // Given
        createPresenter()
        startExploring()
        tapTracker()
        // When
        eventBus.publish(PlayerVictory(playerId = "human"))
        eventBus.dispatch()
        // Then
        questDetailView.visible.shouldBeFalse()
        playerCallToActionView.displayedActions shouldBe null
    }

    @Test
    fun `Tick should not apply the terrain effects while reading the quest`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        tapTracker()
        val requests = requestedTerrainEffects()
        // When
        presenter.tick(3.seconds)
        eventBus.dispatch()
        // Then
        requests.shouldBeEmpty()
    }

    @Test
    fun `GameRestored should display the virtual pad in place of the battle HUD and the call to action when a saved game is restored`() {
        // Given
        createPresenter()
        // When
        eventBus.publish(SaveEvent.GameRestored(slot = 1))
        eventBus.dispatch()
        // Then
        virtualPadView.visible.shouldBeTrue()
        battleHudView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
    }
}
