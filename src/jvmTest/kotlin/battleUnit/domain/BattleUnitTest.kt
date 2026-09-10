package battleUnit.domain

import ability.domain.AbilityMother
import effect.domain.Effect
import effect.domain.EffectMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import player.domain.PlayerMother
import unit.domain.UnitMother

class BattleUnitTest {
    @Nested
    inner class Deploy {
        @Test
        fun `should deploy the battle unit with the unit stats when deployed on the battlefield`() {
            // Given
            val battleUnitId = "battle-unit-1"
            val unit = UnitMother.unit(healthPoints = 10, manaPoints = 5, movementRange = 3).toDto()
            val player = PlayerMother.player().toDto()
            // When
            val deployedBattleUnit = BattleUnit.deploy(id = battleUnitId, unit = unit, player = player, deployAtRow = 2, deployAtColumn = 3)
            // Then
            val battleUnitDto = deployedBattleUnit.toDto()
            assertThat(battleUnitDto.id).isEqualTo(battleUnitId)
            assertThat(battleUnitDto.unitId).isEqualTo(unit.id)
            assertThat(battleUnitDto.playerId).isEqualTo(player.id)
            assertThat(battleUnitDto.remainingHealthPoints).isEqualTo(10)
            assertThat(battleUnitDto.remainingManaPoints).isEqualTo(5)
            assertThat(battleUnitDto.remainingTurnActions).isEqualTo(BattleUnit.Dto.RemainingTurnActionsDto(remainingCasts = 1, remainingSteps = 3))
        }

        @Test
        fun `should publish the battle unit deployed event when the battle unit is deployed`() {
            // Given
            val battleUnitId = "battle-unit-1"
            val unit = UnitMother.unit().toDto()
            val player = PlayerMother.player().toDto()
            // When
            val deployedBattleUnit = BattleUnit.deploy(id = battleUnitId, unit = unit, player = player, deployAtRow = 2, deployAtColumn = 3)
            // Then
            val (events, _) = deployedBattleUnit.pullEvents()
            assertThat(events)
                .containsExactly(BattleUnitEvent.BattleUnitDeployed(battleUnitId = battleUnitId, row = 2, column = 3))
        }
    }

    @Nested
    inner class Move {
        @Test
        fun `should reduce the remaining steps and publish the moved event when the battle unit moves`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
            // When
            val movedBattleUnit = battleUnit.move(distance = 2, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2)
            // Then
            assertThat(movedBattleUnit.toDto().remainingTurnActions.remainingSteps).isEqualTo(1)
            val (events, _) = movedBattleUnit.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattleUnitEvent.BattleUnitMoved(
                        battleUnitId = movedBattleUnit.toDto().id,
                        fromRow = 0,
                        fromColumn = 0,
                        toRow = 0,
                        toColumn = 2,
                    ),
                )
        }

        @Test
        fun `should fail when the movement distance is not greater than zero`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            // When
            val result = runCatching { battleUnit.move(distance = 0, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 1) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(BattleUnitError.MovementDistanceMustBeGreaterThanZero::class.java)
        }

        @Test
        fun `should fail when the movement distance exceeds the remaining steps`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
            // When
            val result = runCatching { battleUnit.move(distance = 4, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 4) }
            // Then
            assertThat(result.exceptionOrNull()).isExactlyInstanceOf(BattleUnitError.MovementDistanceExceedsRemainingSteps::class.java)
        }
    }

    @Nested
    inner class Teleport {
        @Test
        fun `should publish the moved event and keep the remaining steps when the battle unit is teleported`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
            // When
            val teleportedBattleUnit = battleUnit.teleport(fromRow = 0, fromColumn = 0, toRow = 2, toColumn = 2)
            // Then
            assertThat(teleportedBattleUnit.toDto().remainingTurnActions.remainingSteps).isEqualTo(3)
            val (events, _) = teleportedBattleUnit.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattleUnitEvent.BattleUnitMoved(
                        battleUnitId = teleportedBattleUnit.toDto().id,
                        fromRow = 0,
                        fromColumn = 0,
                        toRow = 2,
                        toColumn = 2,
                    ),
                )
        }
    }

    @Nested
    inner class ResetActions {
        @Test
        fun `should restore the remaining steps and casts when the turn actions are reset`() {
            // Given
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
                    .move(distance = 2, fromRow = 0, fromColumn = 0, toRow = 0, toColumn = 2)
            // When
            val resetBattleUnit = battleUnit.resetActions()
            // Then
            assertThat(resetBattleUnit.toDto().remainingTurnActions)
                .isEqualTo(BattleUnit.Dto.RemainingTurnActionsDto(remainingCasts = 1, remainingSteps = 3))
        }
    }

    @Nested
    inner class ReduceCoolDowns {
        @Test
        fun `should reduce the ability cooldowns when the turn passes`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1")).toDto())
            val battleUnitWithCooldown = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 3, abilityCost = 0, row = 0, column = 0)
            // When
            val updatedBattleUnit = battleUnitWithCooldown.reduceCoolDowns()
            // Then
            assertThat(updatedBattleUnit.toDto().abilityCooldowns).containsEntry("ability-1", 2)
        }
    }

    @Nested
    inner class ReplenishMana {
        @Test
        fun `should replenish the remaining mana points up to the unit maximum when mana is replenished`() {
            // Given
            val unit = UnitMother.unit(manaPoints = 10).toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = unit)
                    .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 5, row = 0, column = 0)
            // When
            val replenishedBattleUnit = battleUnit.replenishMana(unit = unit)
            // Then
            assertThat(replenishedBattleUnit.toDto().remainingManaPoints).isEqualTo(10)
        }
    }

    @Nested
    inner class CanMoveDistance {
        @Test
        fun `should be true when the distance is within the remaining steps`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
            // When
            val result = battleUnit.canMoveDistance(distance = 2)
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should be false when the distance exceeds the remaining steps`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
            // When
            val result = battleUnit.canMoveDistance(distance = 4)
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class IsSamePlayer {
        @Test
        fun `should be true when both battle units belong to the same player`() {
            // Given
            val player = PlayerMother.player().toDto()
            val battleUnit = BattleUnitMother.battleUnit(player = player)
            val otherBattleUnit = BattleUnitMother.battleUnit(player = player)
            // When
            val result = battleUnit.isSamePlayer(otherBattleUnit)
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should be false when the battle units belong to different players`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(player = PlayerMother.player(id = "player-1").toDto())
            val otherBattleUnit = BattleUnitMother.battleUnit(player = PlayerMother.player(id = "player-2").toDto())
            // When
            val result = battleUnit.isSamePlayer(otherBattleUnit)
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class CanCastAbility {
        @Test
        fun `should be true when the battle unit has actions, no cooldown and enough mana`() {
            // Given
            val ability = AbilityMother.ability(id = "ability-1", cost = 3, cooldown = 5).toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
            // When
            val result = battleUnit.canCastAbility(ability = ability)
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should be false when the ability is on cooldown`() {
            // Given
            val ability = AbilityMother.ability(id = "ability-1", cost = 3, cooldown = 5).toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
                    .castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityCost = 0, row = 0, column = 0)
            // When
            val result = battleUnit.canCastAbility(ability = ability)
            // Then
            assertThat(result).isFalse
        }

        @Test
        fun `should be false when the battle unit has no remaining casts`() {
            // Given
            val ability = AbilityMother.ability(id = "ability-1", cost = 3, cooldown = 5).toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1", "ability-2"), manaPoints = 10).toDto())
                    .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 0, row = 0, column = 0)
            // When
            val result = battleUnit.canCastAbility(ability = ability)
            // Then
            assertThat(result).isFalse
        }

        @Test
        fun `should be false when the ability is not in the battle unit`() {
            // Given
            val ability = AbilityMother.ability(id = "ability-2", cost = 3, cooldown = 5).toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
            // When
            val result = battleUnit.canCastAbility(ability = ability)
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class CastAbility {
        @Test
        fun `should reduce the mana and casts, set the cooldown and publish the event when the ability is cast`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
            // When
            val updatedBattleUnit = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityCost = 3, row = 1, column = 1)
            // Then
            val battleUnitDto = updatedBattleUnit.toDto()
            assertThat(battleUnitDto.remainingManaPoints).isEqualTo(7)
            assertThat(battleUnitDto.remainingTurnActions.remainingCasts).isEqualTo(0)
            assertThat(battleUnitDto.abilityCooldowns).containsEntry("ability-1", 2)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events)
                .containsExactly(BattleUnitEvent.AbilityCasted(battleUnitId = battleUnitDto.id, abilityId = "ability-1", row = 1, column = 1))
        }
    }

    @Nested
    inner class ReceiveDelayedEffect {
        @Test
        fun `should add the delayed effect and publish the received event when the battle unit receives it`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            val effectId = "effect-1"
            // When
            val updatedBattleUnit = battleUnit.receiveDelayedEffect(effectId = effectId, turnsLeft = 2)
            // Then
            assertThat(updatedBattleUnit.toDto().ongoingEffects.delayedEffects).containsExactly(effectId)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).containsExactly(BattleUnitEvent.EffectReceived(battleUnitId = updatedBattleUnit.toDto().id, effectId = effectId))
        }
    }

    @Nested
    inner class IsDefeated {
        @Test
        fun `should be false when the battle unit still has health points`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(healthPoints = 10).toDto())
            // When
            val result = battleUnit.isDefeated()
            // Then
            assertThat(result).isFalse
        }

        @Test
        fun `should be true when the battle unit has no health points left`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 1).toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = unit)
                    .applyImmediateEffect(effect = EffectMother.effect(power = 1).toDto(), unit = unit)
            // When
            val result = battleUnit.isDefeated()
            // Then
            assertThat(result).isTrue
        }
    }

    @Nested
    inner class ApplyImmediateEffect {
        @Test
        fun `should reduce the health points and publish the damaged event when the effect decreases health`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = unit)
            // When
            val updatedBattleUnit = battleUnit.applyImmediateEffect(effect = EffectMother.effect(power = 3).toDto(), unit = unit)
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(7)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).containsExactly(BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id))
        }

        @Test
        fun `should publish the defeated event when the battle unit health points reach zero`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 3).toDto()
            val player = PlayerMother.player(id = "player-1").toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = unit, player = player)
            // When
            val updatedBattleUnit = battleUnit.applyImmediateEffect(effect = EffectMother.effect(power = 3).toDto(), unit = unit)
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(0)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id),
                    BattleUnitEvent.BattleUnitDefeated(playerId = player.id, battleUnitId = updatedBattleUnit.toDto().id),
                )
        }

        @Test
        fun `should increase the health points up to the unit maximum and publish the healed event when the effect increases health`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = unit)
                    .applyImmediateEffect(effect = EffectMother.effect(power = 4).toDto(), unit = unit)
                    .pullEvents()
                    .second
            val healingEffect = EffectMother.effect(power = 20).toDto().copy(type = Effect.Dto.TypeDto.INCREASE_HEALTH)
            // When
            val updatedBattleUnit = battleUnit.applyImmediateEffect(effect = healingEffect, unit = unit)
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(10)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).containsExactly(BattleUnitEvent.BattleUnitHealed(battleUnitId = updatedBattleUnit.toDto().id))
        }

        @Test
        fun `should publish the teleported event when the effect teleports the battle unit`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = unit)
            val teleportEffect = EffectMother.effect(power = 0).toDto().copy(type = Effect.Dto.TypeDto.TELEPORT)
            // When
            val updatedBattleUnit = battleUnit.applyImmediateEffect(effect = teleportEffect, unit = unit)
            // Then
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).containsExactly(BattleUnitEvent.BattleUnitTeleported(battleUnitId = updatedBattleUnit.toDto().id))
        }
    }

    @Nested
    inner class HasDelayedOngoingEffects {
        @Test
        fun `should be true when the battle unit has delayed effects`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit().receiveDelayedEffect(effectId = "effect-1", turnsLeft = 2)
            // When
            val result = battleUnit.hasDelayedOngoingEffects()
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should be false when the battle unit has no delayed effects`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            // When
            val result = battleUnit.hasDelayedOngoingEffects()
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class ApplyDelayedEffect {
        @Test
        fun `should reduce the health points and remove the delayed effect when the effect decreases health`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val delayedEffect = EffectMother.effect(power = 3).toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = unit).receiveDelayedEffect(effectId = delayedEffect.id, turnsLeft = 1)
            // When
            val updatedBattleUnit = battleUnit.applyDelayedEffect(effect = delayedEffect)
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(7)
            assertThat(updatedBattleUnit.toDto().ongoingEffects.delayedEffects).isEmpty()
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).contains(BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id))
        }
    }
}
