package battleUnit.usecases.commands

import ability.domain.Ability
import ability.domain.AbilityMother.ability
import ability.usecases.queries.SearchAbilityById
import battleUnit.adapters.storage.InMemoryBattleUnitRepository
import battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import battleUnit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import battleUnit.domain.BattleUnitEvent
import battleUnit.domain.BattleUnitMother.battleUnit
import battlefield.usecases.queries.SearchOccupant
import battlefield.usecases.queries.SearchPosition
import effect.domain.EffectMother.effect
import effect.usecases.queries.SearchEffectById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import player.domain.PlayerMother.player
import shared.domain.FakeEventBus
import shared.domain.assertThat
import unit.domain.UnitMother.unit
import unit.usecases.queries.SearchUnitById

class ReceiveAbilityEffectsTest {
    private val searchAbilityById: SearchAbilityById = mock()
    private val searchEffectById: SearchEffectById = mock()
    private val searchOccupant: SearchOccupant = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPosition: SearchPosition = mock()
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val receiveAbilityEffects =
        ReceiveAbilityEffects(
            searchAbilityById = searchAbilityById,
            searchEffectById = searchEffectById,
            battleUnitRepository = battleUnitRepository,
            eventBus = eventBus,
            searchOccupant = searchOccupant,
            searchUnitById = searchUnitById,
            searchPosition = searchPosition,
        )

    @Test
    fun `should apply immediate effect when the ability targets self`() {
        // Given
        val effectId = "effect-1"
        val effect = effect(id = effectId, power = 3, applicationType = "IMMEDIATELY").toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(id = "ability-1", targetPattern = Ability.Dto.TargetPatternDto.SELF, effects = listOf(effectId))
                .toDto(),
        )
        whenever(searchEffectById(effectId)).thenReturn(effect)
        whenever(searchOccupant(0, 0)).thenReturn(battleUnitId)
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        // When
        receiveAbilityEffects(battleUnitId = battleUnitId, abilityId = "ability-1", row = 0, column = 0)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto()
        assertThat(storedBattleUnit!!.remainingHealthPoints).isEqualTo(7)
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.EffectReceived(battleUnitId, effectId),
            BattleUnitEvent.BattleUnitDamaged(battleUnitId),
        )
    }

    @Test
    fun `should throw ability does not exist when the ability is unknown`() {
        // Given
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("unknown-ability")).thenReturn(null)
        // When
        val error =
            catchThrowable {
                receiveAbilityEffects(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability", row = 0, column = 0)
            }
        // Then
        assertThat(error).isInstanceOf(AbilityDoesNotExists::class.java)
    }

    @Test
    fun `should throw failed to receive error when the self tile has no occupant`() {
        // Given
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("ability-1")).thenReturn(
            ability(id = "ability-1", targetPattern = Ability.Dto.TargetPatternDto.SELF, effects = listOf("effect-1"))
                .toDto(),
        )
        whenever(searchEffectById("effect-1")).thenReturn(
            effect(id = "effect-1")
                .toDto(),
        )
        whenever(searchOccupant(0, 0)).thenReturn(null)
        // When
        val error =
            catchThrowable {
                receiveAbilityEffects(battleUnitId = battleUnit.toDto().id, abilityId = "ability-1", row = 0, column = 0)
            }
        // Then
        assertThat(error).isInstanceOf(FailedToReceiveAbilityEffects::class.java)
    }

    @Test
    fun `should not receive effects when the battle unit does not exist`() {
        // Given
        // When
        receiveAbilityEffects(battleUnitId = "unknown-battle-unit", abilityId = "ability-1", row = 0, column = 0)
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
