package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.inventory.domain.InventoryEvent
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumable
import com.mkz.rpg.lootableItem.domain.LootableItemMother.consumableLootableItem
import com.mkz.rpg.npc.domain.NpcMother.npc
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.HUMAN
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.battleSceneTimeline
import com.mkz.rpg.screen.adapters.presentation.BattleSceneTimelineMother.playLongStep
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud.Dto.TileDto
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.adapters.presentation.SubdomainApis
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.math.geom.Size
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ExplorationPresenterTest {
    // The knight explores the sand from (1,1), next to The Guy, who stands west of it at (1,0). The merchant stands at
    // (2,2), selling a health potion and a mana potion. There is no battle yet.
    private val apis =
        SubdomainApis(
            terrains = listOf(occupiableTerrain(id = "sand").toDto()),
            effects = listOf(increaseHealthEffect(id = "cure").toDto()),
            lootableItems =
                listOf(
                    consumableLootableItem(id = "health-potion", name = "Health Potion", price = 10, consumable = consumable(effectIds = setOf("cure"))).toDto(),
                    consumableLootableItem(id = "mana-potion", name = "Mana Potion", price = 15, consumable = consumable(effectIds = setOf("cure"))).toDto(),
                ),
            units = listOf(unit(id = "knight").toDto()),
            players = listOf(player(id = "human", type = HUMAN).toDto(), player(id = "cpu", type = CPU).toDto()),
            tiles = List(3) { List(3) { "sand" } },
            deployments = listOf(Deployment(battleUnitId = "knight-1", unitId = "knight", playerId = "human", row = 1, column = 1)),
            npcs =
                listOf(
                    npc(id = "the-guy", name = "The Guy", line = "G'day bloke!", row = 1, column = 0).toDto(),
                    npc(id = "merchant", name = "Merchant", line = "How can I help you?", row = 2, column = 2, stock = listOf("health-potion", "mana-potion")).toDto(),
                ),
        ).also(SubdomainApis::initialise)
    private val eventBus = apis.eventBus
    private val virtualPadView = VirtualPadView()
    private val npcDialogView = NpcDialogView(Size(390, 300), BattleSceneResourcesLoader())
    private val shopView = ShopView(BattleSceneResourcesLoader())
    private val battlefieldView = FakeBattlefieldView()
    private val battleHudView = FakeBattleHudView()
    private val playerCallToActionView = FakePlayerCallToActionView()
    private val timeline = battleSceneTimeline()
    private val shopPresenter =
        ShopPresenter(
            shopView,
            battlefieldView,
            apis.partyApi,
            apis.inventoryApi,
            apis.lootableItemApi,
            apis.battleUnitApi,
            apis.unitApi,
            apis.battlefieldApi,
            timeline,
            eventBus,
        )

    private fun createPresenter() = ExplorationPresenter(virtualPadView, npcDialogView, battleHudView, playerCallToActionView, apis.partyApi, apis.npcApi, shopPresenter, timeline, eventBus)

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
    fun `DisplayExploring should display the battle HUD and the call to action in place of the virtual pad when the exploration pauses`() {
        // Given
        createPresenter()
        startExploring()
        // When
        eventBus.publish(PartyEvent.ExplorationPaused(playerId = "human"))
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
        eventBus.publish(PartyEvent.ExplorationPaused(playerId = "human"))
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
    fun `WalkParty should walk the party towards the held arrow when the scene is idle`() {
        // Given
        createPresenter()
        startExploring()
        // When
        virtualPadView.holdArrow(DirectionDto.NORTH)
        eventBus.dispatch()
        // Then
        apis.battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
    }

    @Test
    fun `WalkParty should walk the party once the scene is idle when an arrow was tapped while it played`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        // When
        tapArrow(DirectionDto.NORTH)
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
    fun `WalkParty should walk only towards the last arrow tapped while the scene played`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        tapArrow(DirectionDto.NORTH)
        tapArrow(DirectionDto.EAST)
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
    fun `WalkParty should forget the arrow tapped while the scene played when the pad is hidden`() {
        // Given
        val presenter = createPresenter()
        startExploring()
        val longStep = timeline.playLongStep()
        tapArrow(DirectionDto.NORTH)
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

    private fun tapArrow(direction: DirectionDto) {
        virtualPadView.holdArrow(direction)
        virtualPadView.releaseArrow()
    }

    private fun requestedTerrainEffects() =
        mutableListOf<Set<String>>().also { requests ->
            eventBus.subscribe<BattlefieldEvent.RequestEffectApplicationToOccupants> { requests += it.occupantIds }
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
        apis.partyApi.pauseExploration()
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
    fun `WalkParty should not walk the party while the pad is hidden, even when an arrow is still held`() {
        // Given
        createPresenter()
        startExploring()
        eventBus.publish(knightSelected)
        eventBus.dispatch()
        // When
        virtualPadView.holdArrow(DirectionDto.NORTH)
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
        npcDialogView.displayedName() shouldBe "The Guy"
        npcDialogView.displayedLine() shouldBe "G'day bloke!"
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
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "the-guy"
        npcDialogView.visible.shouldBeTrue()
    }

    @Test
    fun `TalkToAdjacentNpc should do nothing when no npc is next to the leader`() {
        // Given
        createPresenter()
        startExploring()
        virtualPadView.holdArrow(DirectionDto.NORTH)
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
    fun `WalkParty should not walk the party while talking, even when an arrow is still held`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        virtualPadView.holdArrow(DirectionDto.NORTH)
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
    fun `DisplayExploring should close the dialog when the exploration pauses while talking`() {
        // Given
        createPresenter()
        startExploring()
        talk()
        // When
        eventBus.publish(PartyEvent.ExplorationPaused(playerId = "human"))
        eventBus.dispatch()
        // Then
        npcDialogView.visible.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
    }

    // The leader walks east to (1,2), next to the merchant, south of it, and talks to it.
    private fun talkToTheMerchant() {
        virtualPadView.holdArrow(DirectionDto.EAST)
        virtualPadView.releaseArrow()
        eventBus.dispatch()
        talk()
    }

    // ... and chooses to buy.
    private fun openTheShop() {
        talkToTheMerchant()
        npcDialogView.selectChoice("Buy")
        eventBus.dispatch()
    }

    @Test
    fun `TalkToAdjacentNpc should display Buy, Sell and Leave on the dialog of a merchant`() {
        // Given
        createPresenter()
        startExploring()
        // When
        talkToTheMerchant()
        // Then
        npcDialogView.choiceLabels().shouldContainExactly("Buy", "Sell", "Leave")
    }

    @Test
    fun `TalkToAdjacentNpc should display no choices on the dialog of an npc that is no merchant`() {
        // Given
        createPresenter()
        startExploring()
        // When
        talk()
        // Then
        npcDialogView.choiceLabels().shouldBeEmpty()
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
        npcDialogView.selectChoice("Buy")
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeTrue()
        shopView.visible.shouldBeTrue()
        shopView.footerText() shouldBe "Tap an item to buy"
        npcDialogView.visible.shouldBeFalse()
        virtualPadView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "merchant"
    }

    @Test
    fun `Sell should open the shop of the merchant to sell`() {
        // Given
        createPresenter()
        startExploring()
        talkToTheMerchant()
        // When
        npcDialogView.selectChoice("Sell")
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
        npcDialogView.selectChoice("Buy")
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
        npcDialogView.selectChoice("Leave")
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
        shopPresenter.shopBackSelected()
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeFalse()
        shopView.visible.shouldBeFalse()
        npcDialogView.visible.shouldBeTrue()
        npcDialogView.choiceLabels().shouldContainExactly("Buy", "Sell", "Leave")
        virtualPadView.visible.shouldBeFalse()
        playerCallToActionView.visible.shouldBeFalse()
        apis.partyApi.searchParty()!!.talkingNpcId shouldBe "merchant"
    }

    @Test
    fun `Sell should open the shop to sell after Back from buying`() {
        // Given
        createPresenter()
        startExploring()
        openTheShop()
        shopPresenter.shopBackSelected()
        eventBus.dispatch()
        // When
        npcDialogView.selectChoice("Sell")
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeTrue()
        shopView.footerText() shouldBe "Nothing to sell"
        npcDialogView.visible.shouldBeFalse()
    }

    @Test
    fun `DisplayExploring should close the shop and display the finish turn again when the exploration pauses while shopping`() {
        // Given
        createPresenter()
        startExploring()
        openTheShop()
        shopPresenter.shopSlotSelected(0)
        // When
        eventBus.publish(PartyEvent.ExplorationPaused(playerId = "human"))
        eventBus.dispatch()
        // Then
        shopPresenter.isOpen.shouldBeFalse()
        battleHudView.visible.shouldBeTrue()
        playerCallToActionView.visible.shouldBeTrue()
        playerCallToActionView.displayedItemActions shouldBe null
    }
}
