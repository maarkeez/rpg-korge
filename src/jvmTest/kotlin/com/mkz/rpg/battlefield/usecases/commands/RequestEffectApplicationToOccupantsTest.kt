package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import org.junit.jupiter.api.Test

class RequestEffectApplicationToOccupantsTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository()
    private val eventBus = FakeEventBus()
    private val requestEffectApplicationToOccupants =
        RequestEffectApplicationToOccupants(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = SearchTerrainById(terrainRepository),
            eventBus = eventBus,
        )

    @Test
    fun `should request the effect application to the occupant when the tile terrain has an effect assigned`() {
        // Given
        val occupiedBattlefield = battlefield().occupy(1, 1, "battle-unit-1").pullEvents().second
        battlefieldRepository.create(occupiedBattlefield)
        terrainRepository.create(Terrain.create(Terrain.Dto(id = "sand", canBeOccupied = true, effectId = "effect-1")))
        // When
        requestEffectApplicationToOccupants()
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.terrain("sand"),
                        target = EffectTargetDto.Unit(id = "battle-unit-1"),
                        effectId = "effect-1",
                    ),
            ),
        )
    }

    @Test
    fun `should request one effect application per occupied tile when multiple tiles are occupied on the terrain with an effect`() {
        // Given
        val occupiedBattlefield =
            battlefield()
                .occupy(0, 0, "battle-unit-1")
                .occupy(2, 2, "battle-unit-2")
                .pullEvents()
                .second
        battlefieldRepository.create(occupiedBattlefield)
        terrainRepository.create(Terrain.create(Terrain.Dto(id = "sand", canBeOccupied = true, effectId = "effect-1")))
        // When
        requestEffectApplicationToOccupants()
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.terrain("sand"),
                        target = EffectTargetDto.Unit(id = "battle-unit-1"),
                        effectId = "effect-1",
                    ),
            ),
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.terrain("sand"),
                        target = EffectTargetDto.Unit(id = "battle-unit-2"),
                        effectId = "effect-1",
                    ),
            ),
        )
    }

    @Test
    fun `should not request effect applications when the battlefield has no occupant`() {
        // Given
        battlefieldRepository.create(battlefield())
        terrainRepository.create(Terrain.create(Terrain.Dto(id = "sand", canBeOccupied = true, effectId = "effect-1")))
        // When
        requestEffectApplicationToOccupants()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not request effect applications when the terrain has no effect assigned`() {
        // Given
        val occupiedBattlefield = battlefield().occupy(1, 1, "battle-unit-1").pullEvents().second
        battlefieldRepository.create(occupiedBattlefield)
        terrainRepository.create(Terrain.create(Terrain.Dto(id = "sand", canBeOccupied = true)))
        // When
        requestEffectApplicationToOccupants()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not request effect applications when the battlefield does not exist`() {
        // When
        requestEffectApplicationToOccupants()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
