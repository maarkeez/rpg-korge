package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.random.Random

class ApplyEffectTest {
    private val searchEffectById: SearchEffectById = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPosition: SearchPosition = mock()

    private val deployEffect =
        Effect.Dto(
            id = "deploy-bee",
            outcome =
                EffectOutcomeDto(
                    type = DEPLOY_BATTLE_UNIT,
                    decreaseHealth = null,
                    increaseHealth = null,
                    applyEffectOnNearbyAllies = null,
                    deployBattleUnit = EffectOutcomeDto.DeployBattleUnitDto(unitId = "bee"),
                ),
            application =
                ApplicationDto(
                    type = ApplicationTypeDto.IMMEDIATELY,
                    onTurnStarted = null,
                    beforeApplyingEffect = null,
                ),
        )

    @Nested
    inner class Invoke {
        @Test
        fun `should publish a deploy request for the effect unit when applied to a tile`() {
            // Given
            val eventBus = FakeEventBus()
            val battleUnitRepository = InMemoryBattleUnitRepository()
            val caster = battleUnit(id = "caster")
            battleUnitRepository.create(caster)
            whenever(searchEffectById("deploy-bee")).thenReturn(deployEffect)
            val applyEffect =
                ApplyEffect(
                    battleUnitRepository,
                    eventBus,
                    searchEffectById,
                    searchUnitById,
                    searchPosition,
                )
            // When
            applyEffect(
                EffectApplicationDto(
                    source = ApplicationSourceDto.battleUnit(caster.toDto().id),
                    target = EffectTargetDto.Tile(row = 2, column = 3),
                    effectId = "deploy-bee",
                ),
            )
            // Then
            val deployed = eventBus.publishedEvents.filterIsInstance<BattleUnitEvent.RequestDeployBattleUnit>()
            assertThat(deployed).hasSize(1)
            assertThat(deployed.first().unitId).isEqualTo("bee")
            assertThat(deployed.first().playerId).isEqualTo(caster.toDto().playerId)
            assertThat(deployed.first().deployAtRow).isEqualTo(2)
            assertThat(deployed.first().deployAtColumn).isEqualTo(3)
        }

        @Test
        fun `should publish the same deployed unit id when the same seed is used`() {
            // Given
            val firstRepository = InMemoryBattleUnitRepository()
            firstRepository.create(battleUnit(id = "caster", player = player(id = "player-1").toDto()))
            val secondRepository = InMemoryBattleUnitRepository()
            secondRepository.create(battleUnit(id = "caster", player = player(id = "player-1").toDto()))
            val firstEventBus = FakeEventBus()
            val secondEventBus = FakeEventBus()
            whenever(searchEffectById("deploy-bee")).thenReturn(deployEffect)
            val application =
                EffectApplicationDto(
                    source = ApplicationSourceDto.battleUnit("caster"),
                    target = EffectTargetDto.Tile(row = 2, column = 3),
                    effectId = "deploy-bee",
                )
            // When
            ApplyEffect(firstRepository, firstEventBus, searchEffectById, searchUnitById, searchPosition, Random(seed = 7))(application)
            ApplyEffect(secondRepository, secondEventBus, searchEffectById, searchUnitById, searchPosition, Random(seed = 7))(application)
            // Then
            assertThat(firstEventBus.publishedEvents).isEqualTo(secondEventBus.publishedEvents)
        }
    }
}
