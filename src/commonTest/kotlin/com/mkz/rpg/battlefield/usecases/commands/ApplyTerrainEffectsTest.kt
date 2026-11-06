package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.terrain.adapters.storage.InMemoryTerrainRepository
import com.mkz.rpg.terrain.domain.TerrainMother.occupiableTerrain
import com.mkz.rpg.terrain.usecases.queries.SearchTerrainById
import kotlin.test.Test

class ApplyTerrainEffectsTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val terrainRepository = InMemoryTerrainRepository()
    private val eventBus = FakeEventBus()
    private val applyTerrainEffects =
        ApplyTerrainEffects(
            battlefieldRepository = battlefieldRepository,
            searchTerrainById = SearchTerrainById(terrainRepository),
            eventBus = eventBus,
        )

    @Test
    fun `should apply the terrain effect to the occupant when the tile terrain has an effect assigned`() {
        // Given
        val occupiedBattlefield = battlefield().occupy(1, 1, "battle-unit-1").pullEvents().second
        battlefieldRepository.create(occupiedBattlefield)
        terrainRepository.create(occupiableTerrain(id = "sand", effectId = "effect-1"))
        // When
        applyTerrainEffects(occupantIds = setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.TerrainEffectApplied(
                application =
                    EffectApplicationDto(
                        source = SourceDto.terrain("sand"),
                        recipient = RecipientDto.battleUnit("battle-unit-1"),
                        effectId = "effect-1",
                        destination = null,
                    ),
            ),
        )
    }

    @Test
    fun `should apply one terrain effect per occupied tile when multiple tiles are occupied on the terrain with an effect`() {
        // Given
        val occupiedBattlefield =
            battlefield()
                .occupy(0, 0, "battle-unit-1")
                .occupy(2, 2, "battle-unit-2")
                .pullEvents()
                .second
        battlefieldRepository.create(occupiedBattlefield)
        terrainRepository.create(occupiableTerrain(id = "sand", effectId = "effect-1"))
        // When
        applyTerrainEffects(occupantIds = setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.TerrainEffectApplied(
                application =
                    EffectApplicationDto(
                        source = SourceDto.terrain("sand"),
                        recipient = RecipientDto.battleUnit("battle-unit-1"),
                        effectId = "effect-1",
                        destination = null,
                    ),
            ),
            BattlefieldEvent.TerrainEffectApplied(
                application =
                    EffectApplicationDto(
                        source = SourceDto.terrain("sand"),
                        recipient = RecipientDto.battleUnit("battle-unit-2"),
                        effectId = "effect-1",
                        destination = null,
                    ),
            ),
        )
    }

    @Test
    fun `should only apply the terrain effect to the given battle units when other battle units occupy the terrain with an effect`() {
        // Given
        val occupiedBattlefield =
            battlefield()
                .occupy(0, 0, "battle-unit-1")
                .occupy(2, 2, "battle-unit-3")
                .pullEvents()
                .second
        battlefieldRepository.create(occupiedBattlefield)
        terrainRepository.create(occupiableTerrain(id = "sand", effectId = "effect-1"))
        // When
        applyTerrainEffects(occupantIds = setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.TerrainEffectApplied(
                application =
                    EffectApplicationDto(
                        source = SourceDto.terrain("sand"),
                        recipient = RecipientDto.battleUnit("battle-unit-1"),
                        effectId = "effect-1",
                        destination = null,
                    ),
            ),
        )
    }

    @Test
    fun `should not apply terrain effects when the battlefield has no occupant`() {
        // Given
        battlefieldRepository.create(battlefield())
        terrainRepository.create(occupiableTerrain(id = "sand", effectId = "effect-1"))
        // When
        applyTerrainEffects(occupantIds = setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not apply terrain effects when the terrain has no effect assigned`() {
        // Given
        val occupiedBattlefield = battlefield().occupy(1, 1, "battle-unit-1").pullEvents().second
        battlefieldRepository.create(occupiedBattlefield)
        terrainRepository.create(occupiableTerrain(id = "sand"))
        // When
        applyTerrainEffects(occupantIds = setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not apply terrain effects when the battlefield does not exist`() {
        // When
        applyTerrainEffects(occupantIds = setOf("battle-unit-1", "battle-unit-2"))
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
