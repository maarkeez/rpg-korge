package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.gameSetup.adapters.presentation.GameSetupApi
import com.mkz.tessavar.inventory.adapters.presentation.InventoryApi
import com.mkz.tessavar.lootDrop.adapters.presentation.LootDropApi
import com.mkz.tessavar.lootDrop.adapters.resources.ResourcesLootDropLoader
import com.mkz.tessavar.lootDrop.domain.LootDrop
import com.mkz.tessavar.lootTable.adapters.presentation.LootTableApi
import com.mkz.tessavar.lootTable.adapters.resources.ResourcesLootTableLoader
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.tessavar.npc.adapters.presentation.NpcApi
import com.mkz.tessavar.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class RollLootDropAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val lootableItemApi = LootableItemApi(effectApi, eventBus, ResourcesLootableItemLoader())
    private val lootTableApi = LootTableApi(lootableItemApi, eventBus, ResourcesLootTableLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val lootDropApi = LootDropApi(unitApi, lootTableApi, eventBus, ResourcesLootDropLoader(), Random.Default)
    private val battleApi = BattleApi(eventBus)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val npcApi = NpcApi(battlefieldApi, lootableItemApi, partyApi, eventBus, ResourcesNpcLoader())
    private val inventoryApi = InventoryApi(effectApi, lootableItemApi, unitApi, battleApi, partyApi, npcApi, eventBus)
    private val gameSetupApi = GameSetupApi(eventBus)

    private val playerOneKnightBattleUnitId = "hero"
    private val playerTwoGoatWizardBattleUnitId = "goat-wizard-1"

    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        lootableItemApi.init()
        lootTableApi.init()
        abilityApi.init()
        unitApi.init()
        lootDropApi.init()
        playerApi.init()
        battlefieldApi.init()
        battleUnitApi.init()
        gameSetupApi.setUpGame()
        eventBus.dispatch()
        battleUnitApi.deployBattleUnit(
            battleUnitId = playerTwoGoatWizardBattleUnitId,
            unitId = "goat-wizard",
            playerId = "player-two",
            deployAtRow = 2,
            deployAtColumn = 2,
        )
        eventBus.dispatch()
    }

    private fun defeatTheGoatWizard() =
        repeat(15) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = SourceDto.terrain("lava"),
                    recipient = RecipientDto.battleUnit(playerTwoGoatWizardBattleUnitId),
                    effectId = "low-physical-damage",
                    destination = null,
                ),
            )
        }

    @Test
    fun `should leave a bee essence on its tile when the goat wizard is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchPosition(playerTwoGoatWizardBattleUnitId) == PositionDto(row = 2, column = 2))
            require(battleUnitApi.searchBattleUnitById(playerTwoGoatWizardBattleUnitId)!!.remainingHealth == 150)
            defeatTheGoatWizard()
            // When
            eventBus.dispatch()
            // Then
            lootDropApi.searchDroppedItems() shouldBe mapOf(LootDrop.Dto.PositionDto(row = 2, column = 2) to listOf("bee-essence"))
        }

    @Test
    fun `should store the bee essence of the defeated goat wizard in the inventory and clear its tile when the knight walks onto it`() =
        scenarioTest(::setUp) {
            // Given
            defeatTheGoatWizard()
            eventBus.dispatch()
            require(lootDropApi.searchDroppedItems() == mapOf(LootDrop.Dto.PositionDto(row = 2, column = 2) to listOf("bee-essence")))
            battleUnitApi.walkBattleUnitOutOfBattle(battleUnitId = playerOneKnightBattleUnitId, walkToRow = 2, walkToColumn = 2)
            // When
            eventBus.dispatch()
            // Then
            inventoryApi.searchInventoryByBattleUnitId(playerOneKnightBattleUnitId)!!.storedItems shouldBe listOf("bee-essence")
            lootDropApi.searchDroppedItems().shouldBeEmpty()
        }
}
