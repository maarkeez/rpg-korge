package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battleUnit.domain.BattleUnitError
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.building.adapters.presentation.BuildingApi
import com.mkz.tessavar.building.domain.Building
import com.mkz.tessavar.building.usecases.services.FakeBuildingLoader
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.party.domain.Party.Dto.DirectionDto
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

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

    private var fortress = Building.Dto(id = "fortress", row = 0, column = 2, rows = 2, columns = 2)
    private val buildingApi by lazy { BuildingApi(battlefieldApi, eventBus, FakeBuildingLoader(listOf(fortress))) }

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
        buildingApi.placeBuildings()
        eventBus.dispatch()
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-2", "knight", "human", deployAtRow = 1, deployAtColumn = 1)
        partyApi.startExploration()
        eventBus.dispatch()
    }

    private fun walk(direction: DirectionDto) {
        partyApi.stepParty(direction)
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
            battlefieldApi.isTileVacant(row = 1, column = 3).shouldBeFalse()
        }

    @Test
    fun `should not let the party step into the building`() =
        scenarioTest({ setUp() }) {
            // When
            walk(DirectionDto.EAST)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
            battlefieldApi.searchPosition("knight-2") shouldBe PositionDto(row = 1, column = 1)
        }

    @Test
    fun `should let the party step around the building`() =
        scenarioTest({ setUp() }) {
            // When
            walk(DirectionDto.SOUTH)
            walk(DirectionDto.SOUTH)
            walk(DirectionDto.EAST)
            walk(DirectionDto.EAST)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 2, column = 3)
        }

    @Test
    fun `should fail to deploy a battle unit on a tile of the building`() =
        scenarioTest({ fortress = fortress.copy(column = 1) }) {
            // When
            val result = runCatching { setUp() }
            // Then
            result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.TileIsNotVacant>()
        }
}
