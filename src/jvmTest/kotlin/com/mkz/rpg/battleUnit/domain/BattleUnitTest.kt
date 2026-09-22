package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.position
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.player.domain.PlayerMother
import com.mkz.rpg.unit.domain.UnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
            val battleUnitWithCooldown = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 3, abilityCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
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
                    .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 5, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
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
                    .castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
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
                    .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
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

        @Test
        fun `should not be able to cast when the battle unit has no enough mana`() {
            // Given
            val ability = AbilityMother.ability(cost = 3).toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf(ability.id), manaPoints = 0).toDto())
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
            val castGroup = listOf(Battlefield.Dto.PositionDto(1, 1))
            val updatedBattleUnit = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityCost = 3, castGroup = castGroup)
            // Then
            val battleUnitDto = updatedBattleUnit.toDto()
            assertThat(battleUnitDto.remainingManaPoints).isEqualTo(7)
            assertThat(battleUnitDto.remainingTurnActions.remainingCasts).isEqualTo(0)
            assertThat(battleUnitDto.abilityCooldowns).containsEntry("ability-1", 2)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events)
                .containsExactly(BattleUnitEvent.AbilityCasted(battleUnitId = battleUnitDto.id, abilityId = "ability-1", castGroup = castGroup))
        }
    }

    @Nested
    inner class ReceiveDelayedEffect {
        @Test
        fun `should add the on turn started effect and publish the received event when the battle unit receives it`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            val effectId = "effect-1"
            // When
            val updatedBattleUnit = battleUnit.receiveOnTurnStartedEffect(effectId = effectId, turnsLeft = 2)
            // Then
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onTurnStarted).containsExactly(effectId)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).containsExactly(BattleUnitEvent.EffectReceived(battleUnitId = updatedBattleUnit.toDto().id, effectId = effectId))
        }
    }

    @Nested
    inner class ReceiveOnDefeatedEffect {
        @Test
        fun `should add the on defeated effect and publish the received event when the battle unit receives it`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            val effectId = "effect-1"
            // When
            val updatedBattleUnit = battleUnit.receiveOnDefeatedEffect(effectId = effectId)
            // Then
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onDefeatedEffects).containsExactly(effectId)
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
                    .applyImmediateEffect(
                        effect = EffectMother.decreaseHealthEffect(damage = 1).toDto(),
                        unit = unit,
                        currentRow = position().row,
                        currentColumn = position().column,
                    )
            // When
            val result = battleUnit.isDefeated()
            // Then
            assertThat(result).isTrue
        }
    }

    @Nested
    inner class ApplyOnDefeatedEffects {
        @Test
        fun `should remove the on defeated effects when the on defeated effects are applied`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit().receiveOnDefeatedEffect(effectId = "effect-1")
            // When
            val updatedBattleUnit = battleUnit.applyOnDefeatedEffects()
            // Then
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onDefeatedEffects).isEmpty()
        }

        @Test
        fun `should keep the on turn started effects when the on defeated effects are applied`() {
            // Given
            val battleUnit =
                BattleUnitMother
                    .battleUnit()
                    .receiveOnDefeatedEffect(effectId = "effect-1")
                    .receiveOnTurnStartedEffect(effectId = "effect-2", turnsLeft = 2)
            // When
            val updatedBattleUnit = battleUnit.applyOnDefeatedEffects()
            // Then
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onDefeatedEffects).isEmpty()
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onTurnStarted).containsExactly("effect-2")
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
            val updatedBattleUnit =
                battleUnit.applyImmediateEffect(
                    effect = EffectMother.decreaseHealthEffect(damage = 3).toDto(),
                    unit = unit,
                    currentRow = position().row,
                    currentColumn = position().column,
                )
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
            val defeatedAtPosition = position()
            // When
            val updatedBattleUnit =
                battleUnit.applyImmediateEffect(
                    effect = EffectMother.decreaseHealthEffect(damage = 3).toDto(),
                    unit = unit,
                    currentRow = defeatedAtPosition.row,
                    currentColumn = defeatedAtPosition.column,
                )
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(0)
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events)
                .containsExactly(
                    BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id),
                    BattleUnitEvent.BattleUnitDefeated(
                        playerId = player.id,
                        battleUnitId = updatedBattleUnit.toDto().id,
                        defeatedAtRow = defeatedAtPosition.row,
                        defeatedAtColumn = defeatedAtPosition.column,
                    ),
                )
        }

        @Test
        fun `should increase the health points up to the unit maximum and publish the healed event when the effect increases health`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = unit)
                    .applyImmediateEffect(
                        effect = EffectMother.decreaseHealthEffect(damage = 4).toDto(),
                        unit = unit,
                        currentRow = position().row,
                        currentColumn = position().column,
                    ).pullEvents()
                    .second
            val healingEffect = EffectMother.increaseHealthEffect(healing = 20).toDto()
            // When
            val updatedBattleUnit =
                battleUnit.applyImmediateEffect(
                    effect = healingEffect,
                    unit = unit,
                    currentRow = position().row,
                    currentColumn = position().column,
                )
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
            val teleportEffect = EffectMother.teleportEffect().toDto()
            // When
            val updatedBattleUnit =
                battleUnit.applyImmediateEffect(
                    effect = teleportEffect,
                    unit = unit,
                    currentRow = position().row,
                    currentColumn = position().column,
                )
            // Then
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).containsExactly(BattleUnitEvent.BattleUnitTeleported(battleUnitId = updatedBattleUnit.toDto().id))
        }
    }

    @Nested
    inner class HasDelayedOngoingEffects {
        @Test
        fun `should be true when the battle unit has on turn started effects`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit().receiveOnTurnStartedEffect(effectId = "effect-1", turnsLeft = 2)
            // When
            val result = battleUnit.hasOnTurnStartedEffects()
            // Then
            assertThat(result).isTrue
        }

        @Test
        fun `should be false when the battle unit has no on turn started effects`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit()
            // When
            val result = battleUnit.hasOnTurnStartedEffects()
            // Then
            assertThat(result).isFalse
        }

        @Test
        fun `should be false when the battle unit only has on defeated effects`() {
            // Given
            val battleUnit = BattleUnitMother.battleUnit().receiveOnDefeatedEffect(effectId = "effect-1")
            // When
            val result = battleUnit.hasOnTurnStartedEffects()
            // Then
            assertThat(result).isFalse
        }
    }

    @Nested
    inner class ApplyDelayedEffect {
        @Test
        fun `should reduce the health points and remove the on turn started effect when the effect decreases health`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val onTurnStartedEffect = EffectMother.decreaseHealthEffect(damage = 3, applicationType = "ON_TURN_STARTED").toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = unit).receiveOnTurnStartedEffect(effectId = onTurnStartedEffect.id, turnsLeft = 1)
            // When
            val updatedBattleUnit =
                battleUnit.applyOnTurnStartedEffect(
                    effect = onTurnStartedEffect,
                    currentRow = position().row,
                    currentColumn = position().column,
                )
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(7)
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onTurnStarted).isEmpty()
            val (events, _) = updatedBattleUnit.pullEvents()
            assertThat(events).contains(BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id))
        }

        @Test
        fun `should keep the on turn started effect with one turn less when the effect has more turns left`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val onTurnStartedEffect = EffectMother.decreaseHealthEffect(id = "effect-1", damage = 3, applicationType = "ON_TURN_STARTED").toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = unit).receiveOnTurnStartedEffect(effectId = onTurnStartedEffect.id, turnsLeft = 2)
            // When
            val updatedBattleUnit =
                battleUnit.applyOnTurnStartedEffect(
                    effect = onTurnStartedEffect,
                    currentRow = position().row,
                    currentColumn = position().column,
                )
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(7)
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onTurnStarted).containsExactly("effect-1")
        }

        @Test
        fun `should remove the on turn started effect when it is applied for its last turn`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val onTurnStartedEffect = EffectMother.decreaseHealthEffect(id = "effect-1", damage = 3, applicationType = "ON_TURN_STARTED").toDto()
            val battleUnit = BattleUnitMother.battleUnit(unit = unit).receiveOnTurnStartedEffect(effectId = onTurnStartedEffect.id, turnsLeft = 2)
            // When
            val updatedBattleUnit =
                battleUnit
                    .applyOnTurnStartedEffect(
                        effect = onTurnStartedEffect,
                        currentRow = position().row,
                        currentColumn = position().column,
                    ).applyOnTurnStartedEffect(
                        effect = onTurnStartedEffect,
                        currentRow = position().row,
                        currentColumn = position().column,
                    )
            // Then
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onTurnStarted).isEmpty()
        }

        @Test
        fun `should only update the matching on turn started effect when the battle unit has several effects`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val appliedEffect = EffectMother.decreaseHealthEffect(id = "effect-2", damage = 3, applicationType = "ON_TURN_STARTED").toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = unit)
                    .receiveOnTurnStartedEffect(effectId = "effect-1", turnsLeft = 2)
                    .receiveOnTurnStartedEffect(effectId = appliedEffect.id, turnsLeft = 2)
            // When
            val updatedBattleUnit =
                battleUnit.applyOnTurnStartedEffect(
                    effect = appliedEffect,
                    currentRow = position().row,
                    currentColumn = position().column,
                )
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(7)
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onTurnStarted).containsExactly("effect-1", "effect-2")
        }

        @Test
        fun `should keep the on turn started effect when an immediate effect with the same id is applied`() {
            // Given
            val unit = UnitMother.unit(healthPoints = 10).toDto()
            val immediateEffect = EffectMother.decreaseHealthEffect(id = "effect-1", damage = 3, applicationType = "IMMEDIATELY").toDto()
            val battleUnit =
                BattleUnitMother
                    .battleUnit(unit = unit)
                    .receiveOnTurnStartedEffect(effectId = immediateEffect.id, turnsLeft = 2)
                    .receiveImmediateEffect(effectId = immediateEffect.id)
            // When
            val updatedBattleUnit =
                battleUnit.applyImmediateEffect(
                    effect = immediateEffect,
                    unit = unit,
                    currentRow = position().row,
                    currentColumn = position().column,
                )
            // Then
            assertThat(updatedBattleUnit.toDto().remainingHealthPoints).isEqualTo(7)
            assertThat(updatedBattleUnit.toDto().ongoingEffects.onTurnStarted).containsExactly("effect-1")
        }
    }
}
