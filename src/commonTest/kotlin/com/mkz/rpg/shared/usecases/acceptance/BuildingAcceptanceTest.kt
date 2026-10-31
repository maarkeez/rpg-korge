package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.building.adapters.presentation.BuildingApi
import com.mkz.rpg.building.domain.Building
import com.mkz.rpg.building.usecases.services.FakeBuildingLoader
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

// Uses the units and terrains defined in the resources: two human knights exploring on sand.
class BuildingAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)

    // A fortress of 2x2 tiles, from (0,2) to (1,3), east of the party.
    private var fortress = Building.Dto(id = "fortress", row = 0, column = 2, rows = 2, columns = 2)
    private val buildingApi by lazy { BuildingApi(battlefieldApi, eventBus, FakeBuildingLoader(listOf(fortress))) }

    // The buildings are placed before the battle units are deployed, as when the battle is set up. The knight-1 leads
    // from (0,1), west of the fortress, and the knight-2 follows it from (1,1).
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation("human", HUMAN)
        battlefieldApi.requestBattlefieldCreation(4, 5, List(4) { List(5) { "sand" } })
        eventBus.dispatch()
        buildingApi.initialiseBuildings()
        eventBus.dispatch()
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-2", "knight", "human", deployAtRow = 1, deployAtColumn = 1)
        partyApi.startExploration()
        eventBus.dispatch()
    }

    private fun walk(direction: DirectionDto) {
        partyApi.walkParty(direction)
        eventBus.dispatch()
    }

    @Test
    fun `should occupy every tile of the building`() =
        scenarioTest({ setUp() }) {
            // Then
            battlefieldApi.searchOccupant(row = 0, column = 2) shouldBe "fortress"
            battlefieldApi.searchOccupant(row = 0, column = 3) shouldBe "fortress"
            battlefieldApi.searchOccupant(row = 1, column = 2) shouldBe "fortress"
            battlefieldApi.searchOccupant(row = 1, column = 3) shouldBe "fortress"
            battlefieldApi.canBattlefieldTileBeOccupied(row = 1, column = 3).shouldBeFalse()
        }

    @Test
    fun `should not let the party walk into the building`() =
        scenarioTest({ setUp() }) {
            // When
            walk(DirectionDto.EAST)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
            battlefieldApi.searchPosition("knight-2") shouldBe PositionDto(row = 1, column = 1)
        }

    @Test
    fun `should let the party walk around the building`() =
        scenarioTest({ setUp() }) {
            // When
            walk(DirectionDto.SOUTH)
            walk(DirectionDto.SOUTH)
            walk(DirectionDto.EAST)
            walk(DirectionDto.EAST)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 2, column = 3)
        }

    // The fortress already occupies its tiles here, so the deployment itself refuses the tile.
    @Test
    fun `should fail to deploy a battle unit on a tile of the building`() =
        scenarioTest({ fortress = fortress.copy(column = 1) }) {
            // When
            val result = runCatching { setUp() }
            // Then
            result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.BattlefieldTileCanNotBeOccupied>()
        }
}
