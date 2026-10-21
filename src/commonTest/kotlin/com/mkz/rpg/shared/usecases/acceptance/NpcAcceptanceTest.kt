package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldError
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.domain.NpcError
import com.mkz.rpg.npc.usecases.services.FakeNpcLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

// Uses the units, terrains and lootable items defined in the resources: two human knights with 100 health points, whose
// poisoned sword hits an adjacent enemy, a CPU rat, the lava, which damages 5 health points, and the potions and the hammer.
class NpcAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val lootableItemApi = LootableItemApi(effectApi, eventBus)
    private val theGuy = Npc.Dto(id = "the-guy", name = "The Guy", line = "G'day bloke!", row = 0, column = 0)

    // Recruitable, as the viking of the resources: 150 health points.
    private val viking = Npc.Dto(id = "viking", name = "Viking", line = "For Valhalla!", row = 0, column = 3, unitId = "viking")

    // A merchant, selling the lootable items of the resources. The leader walks east to (0,2) to stand next to it.
    private val merchant = Npc.Dto(id = "merchant", name = "Merchant", line = "How can I help you?", row = 1, column = 2, stock = listOf("health-potion", "mana-potion", "hammer"))

    // The npcs placed by the set up, The Guy unless a test says otherwise.
    private var npcs = listOf(theGuy)
    private val partyApi by lazy { PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus) }
    private val npcApi by lazy { NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, FakeNpcLoader(npcs)) }
    private val secondsStarted =
        mutableListOf<PartyEvent.ExplorationSecondStarted>().also { seconds -> eventBus.subscribe<PartyEvent.ExplorationSecondStarted> { seconds += it } }

    // The Guy stands on the lava at (0,0). The knight-1 leads from (0,1), east of it, and the knight-2 follows it from
    // (1,1). The rat stands at (7,7), far from the party, so it does not engage.
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        lootableItemApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        lootableItemApi.initialiseLootableItems()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation("human", "Human", HUMAN)
        playerApi.requestPlayerCreation("cpu", "CPU", CPU)
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { row -> List(8) { column -> if (row == 0 && column == 0) "lava" else "sand" } })
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-2", "knight", "human", deployAtRow = 1, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("rat-1", "rat", "cpu", deployAtRow = 7, deployAtColumn = 7)
        npcApi.initialiseNpcs()
        partyApi.startExploration()
        eventBus.dispatch()
    }

    private fun walk(direction: DirectionDto) {
        partyApi.walkParty(direction)
        eventBus.dispatch()
    }

    // A second passes while exploring.
    private fun waitASecond() {
        partyApi.applyStandingTerrainEffects()
        eventBus.dispatch()
    }

    private fun talk() {
        npcApi.talkToAdjacentNpc()
        eventBus.dispatch()
    }

    private fun finishTalk() {
        partyApi.finishTalk()
        eventBus.dispatch()
    }

    // The leader walks east to (0,2), next to the viking at (0,3), and the knight-2 follows it to (0,1).
    private fun recruitTheViking() {
        walk(DirectionDto.EAST)
        talk()
        finishTalk()
    }

    @Test
    fun `should deploy the viking as a battle unit of the human player on its tile, last in the party, once the talk with it is finished`() =
        scenarioTest({
            npcs = listOf(theGuy, viking)
            setUp()
        }) {
            // When
            recruitTheViking()
            // Then
            val vikingBattleUnit = battleUnitApi.searchBattleUnitById("viking")!!
            vikingBattleUnit.playerId shouldBe "human"
            vikingBattleUnit.remainingHealthPoints shouldBe 150
            battlefieldApi.searchOccupant(row = 0, column = 3) shouldBe "viking"
            partyApi.searchParty()!!.battleUnitIds shouldBe listOf("knight-1", "knight-2", "viking")
            npcApi.searchNpcById("viking").shouldBeNull()
        }

    @Test
    fun `should have the recruited viking follow the party when it walks`() =
        scenarioTest({
            npcs = listOf(theGuy, viking)
            setUp()
        }) {
            // Given
            recruitTheViking()
            // When
            walk(DirectionDto.SOUTH)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 1, column = 2)
            battlefieldApi.searchPosition("knight-2") shouldBe PositionDto(row = 0, column = 2)
            battlefieldApi.searchPosition("viking") shouldBe PositionDto(row = 0, column = 1)
        }

    @Test
    fun `should not talk to the viking again once recruited`() =
        scenarioTest({
            npcs = listOf(theGuy, viking)
            setUp()
        }) {
            // Given
            recruitTheViking()
            // When
            talk()
            // Then
            partyApi.searchParty()!!.talkingNpcId.shouldBeNull()
        }

    @Test
    fun `should recruit no one once the talk with an npc that is not recruitable is finished`() =
        scenarioTest(::setUp) {
            // Given
            talk()
            // When
            finishTalk()
            // Then
            battlefieldApi.searchOccupant(row = 0, column = 0) shouldBe "the-guy"
            partyApi.searchParty()!!.battleUnitIds shouldBe listOf("knight-1", "knight-2")
        }

    @Test
    fun `should place a merchant with the items it sells, and talk to it as any other npc`() =
        scenarioTest({
            npcs = listOf(theGuy, merchant)
            setUp()
        }) {
            // Then
            npcApi.searchNpcById("merchant")!!.stock shouldBe listOf("health-potion", "mana-potion", "hammer")
            battlefieldApi.searchOccupant(row = 1, column = 2) shouldBe "merchant"
            // When
            walk(DirectionDto.EAST)
            talk()
            // Then
            partyApi.searchParty()!!.talkingNpcId shouldBe "merchant"
            // When
            finishTalk()
            // Then
            partyApi.searchParty()!!.talkingNpcId.shouldBeNull()
            npcApi.searchNpcById("merchant").shouldNotBeNull()
        }

    @Test
    fun `should fail to place a merchant selling an item that does not exist`() =
        scenarioTest({ npcs = listOf(merchant.copy(stock = listOf("health-potion", "dragon-egg"))) }) {
            // When
            val result = runCatching { setUp() }
            // Then
            result.exceptionOrNull().shouldBeTypeOf<NpcError.NpcStockItemNotFound>()
        }

    @Test
    fun `should have the npc occupy its tile when it is placed`() =
        scenarioTest(::setUp) {
            // Then
            battlefieldApi.searchOccupant(row = 0, column = 0) shouldBe "the-guy"
        }

    @Test
    fun `should not let the party walk onto the tile of the npc`() =
        scenarioTest(::setUp) {
            // When
            walk(DirectionDto.WEST)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
            battlefieldApi.searchOccupant(row = 0, column = 0) shouldBe "the-guy"
        }

    @Test
    fun `should not let a battle unit attack the npc next to it`() =
        scenarioTest(::setUp) {
            // When
            val castGroups = battleUnitApi.whereCanCast("knight-1", "poisoned-sword")
            // Then
            castGroups.shouldBeEmpty()
        }

    @Test
    fun `should not apply the terrain effect to the npc while exploring`() =
        scenarioTest(::setUp) {
            // When
            repeat(2) { waitASecond() }
            // Then
            battlefieldApi.searchOccupant(row = 0, column = 0) shouldBe "the-guy"
            battleUnitApi.searchBattleUnitById("knight-1")!!.remainingHealthPoints shouldBe 100
        }

    @Test
    fun `should fail to place an npc on the tile of a battle unit`() =
        scenarioTest({ npcs = listOf(theGuy.copy(row = 0, column = 1)) }) {
            // When
            val result = runCatching { setUp() }
            // Then
            result.exceptionOrNull().shouldBeTypeOf<BattlefieldError.TileIsNotVacant>()
        }

    @Test
    fun `should talk to the npc next to the leader, and stop the time until the talk is finished`() =
        scenarioTest(::setUp) {
            // When
            talk()
            // Then
            partyApi.searchParty()!!.talkingNpcId shouldBe "the-guy"
            // When
            walk(DirectionDto.EAST)
            waitASecond()
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
            secondsStarted.shouldBeEmpty()
            // When
            finishTalk()
            walk(DirectionDto.EAST)
            waitASecond()
            // Then
            partyApi.searchParty()!!.talkingNpcId.shouldBeNull()
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 2)
            secondsStarted.shouldHaveSize(1)
        }

    @Test
    fun `should not talk when no npc is next to the leader`() =
        scenarioTest(::setUp) {
            // Given
            walk(DirectionDto.EAST)
            // When
            talk()
            // Then
            partyApi.searchParty()!!.talkingNpcId.shouldBeNull()
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
        }
}
