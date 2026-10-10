package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.battlesetup.adapters.serialization.BattleScenarioLoader
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.adapters.events.RecordingEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

/** Test-only battle built from the `ui-showcase` scenario, with every event recorded. Build one per test. */
class ShowcaseBattle {
    val eventBus = RecordingEventBus(InMemoryEventBus())
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, Random(SEED))
    val battleApi = BattleApi(eventBus, battleUnitApi)
    private val battleSetupApi = BattleSetupApi(eventBus)

    init {
        val scenario = runBlocking { BattleScenarioLoader().load(SCENARIO) }
        runBlocking { terrainApi.init() }
        battleSetupApi.setupBattle(scenario)
        eventBus.dispatch()
        eventBus.clear()
    }

    fun fingerprint() =
        BattleStateFingerprint.capture(
            playerIds = listOf(PLAYER_ONE, PLAYER_TWO),
            battleUnitApi = battleUnitApi,
            battlefieldApi = battlefieldApi,
            battleApi = battleApi,
        )

    fun knightAbilityIds(): List<String> =
        battleUnitApi
            .searchBattleUnitById(KNIGHT)!!
            .abilityCooldowns.keys
            .toList()

    fun castGroups(abilityId: String): List<WhereCanCast.CastGroup> = battleUnitApi.whereCanCast(KNIGHT, abilityId)

    fun positionsOf(castGroup: WhereCanCast.CastGroup): List<PositionDto> = castGroup.positions.map { PositionDto(row = it.row, column = it.column) }

    companion object {
        const val KNIGHT = "player-1-unit-1"
        const val SCENARIO = "scenarios/ui-showcase.json"
        private const val PLAYER_ONE = "player-one"
        private const val PLAYER_TWO = "player-two"
        private const val SEED = 42L
    }
}
