package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.position
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.player.domain.PlayerMother
import com.mkz.rpg.unit.domain.UnitMother
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class BattleUnitTest {
    @Test
    fun `Deploy should deploy the battle unit with the unit stats when deployed on the battlefield`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val unit = UnitMother.unit(healthPoints = 10, manaPoints = 5, movementRange = 3).toDto()
        val player = PlayerMother.player().toDto()
        // When
        val deployedBattleUnit = BattleUnit.deploy(id = battleUnitId, unit = unit, player = player, deployAtRow = 2, deployAtColumn = 3)
        // Then
        val battleUnitDto = deployedBattleUnit.toDto()
        battleUnitDto.id shouldBe battleUnitId
        battleUnitDto.unitId shouldBe unit.id
        battleUnitDto.playerId shouldBe player.id
        battleUnitDto.remainingHealthPoints shouldBe 10
        battleUnitDto.remainingManaPoints shouldBe 5
        battleUnitDto.maximumHealthPoints shouldBe 10
        battleUnitDto.maximumManaPoints shouldBe 5
        battleUnitDto.equippedEffects shouldBe emptyMap()
        battleUnitDto.remainingTurnActions shouldBe BattleUnit.Dto.RemainingTurnActionsDto(remainingCasts = 1, remainingSteps = 3, maximumSteps = 3, maximumCasts = 1)
    }

    @Test
    fun `Deploy should publish the battle unit deployed event when the battle unit is deployed`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val unit = UnitMother.unit().toDto()
        val player = PlayerMother.player().toDto()
        // When
        val deployedBattleUnit = BattleUnit.deploy(id = battleUnitId, unit = unit, player = player, deployAtRow = 2, deployAtColumn = 3)
        // Then
        val (events, _) = deployedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitDeployed(battleUnitId = battleUnitId, row = 2, column = 3))
    }

    @Test
    fun `Move should reduce the remaining steps and publish the moved event when the battle unit moves`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
        val path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2))
        // When
        val movedBattleUnit = battleUnit.move(fromRow = 0, fromColumn = 0, path = path)
        // Then
        movedBattleUnit.toDto().remainingTurnActions.remainingSteps shouldBe 1
        val (events, _) = movedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitMoved(
                battleUnitId = movedBattleUnit.toDto().id,
                fromRow = 0,
                fromColumn = 0,
                toRow = 0,
                toColumn = 2,
                path = path,
            ),
        )
    }

    @Test
    fun `Move should fail when the movement distance is not greater than zero`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val result = runCatching { battleUnit.move(fromRow = 0, fromColumn = 0, path = emptyList()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.MovementDistanceMustBeGreaterThanZero>()
    }

    @Test
    fun `Move should fail when the movement distance exceeds the remaining steps`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
        // When
        val path = (1..4).map { column -> Battlefield.Dto.PositionDto(row = 0, column = column) }
        val result = runCatching { battleUnit.move(fromRow = 0, fromColumn = 0, path = path) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.MovementDistanceExceedsRemainingSteps>()
    }

    @Test
    fun `Teleport should publish the moved event and keep the remaining steps when the battle unit is teleported`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
        // When
        val teleportedBattleUnit = battleUnit.teleport(fromRow = 0, fromColumn = 0, toRow = 2, toColumn = 2)
        // Then
        teleportedBattleUnit.toDto().remainingTurnActions.remainingSteps shouldBe 3
        val (events, _) = teleportedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitMoved(
                battleUnitId = teleportedBattleUnit.toDto().id,
                fromRow = 0,
                fromColumn = 0,
                toRow = 2,
                toColumn = 2,
                path = listOf(Battlefield.Dto.PositionDto(row = 2, column = 2)),
            ),
        )
    }

    @Test
    fun `ResetActions should restore the remaining steps and casts when the turn actions are reset`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
                .move(fromRow = 0, fromColumn = 0, path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2)))
        // When
        val resetBattleUnit = battleUnit.resetActions()
        // Then
        resetBattleUnit.toDto().remainingTurnActions shouldBe BattleUnit.Dto.RemainingTurnActionsDto(remainingCasts = 1, remainingSteps = 3, maximumSteps = 3, maximumCasts = 1)
    }

    @Test
    fun `ReduceCoolDowns should reduce the ability cooldowns when the turn passes`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1")).toDto())
        val battleUnitWithCooldown = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 3, abilityCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val updatedBattleUnit = battleUnitWithCooldown.reduceCoolDowns()
        // Then
        updatedBattleUnit.toDto().abilityCooldowns.shouldContain("ability-1", 2)
    }

    @Test
    fun `ReplenishMana should replenish the remaining mana points up to the unit maximum when mana is replenished`() {
        // Given
        val unit = UnitMother.unit(manaPoints = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 5, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val replenishedBattleUnit = battleUnit.replenishMana()
        // Then
        replenishedBattleUnit.toDto().remainingManaPoints shouldBe 10
    }

    @Test
    fun `CanMoveDistance should be true when the distance is within the remaining steps`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
        // When
        val result = battleUnit.canMoveDistance(distance = 2)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `CanMoveDistance should be false when the distance exceeds the remaining steps`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(movementRange = 3).toDto())
        // When
        val result = battleUnit.canMoveDistance(distance = 4)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `IsSamePlayer should be true when both battle units belong to the same player`() {
        // Given
        val player = PlayerMother.player().toDto()
        val battleUnit = BattleUnitMother.battleUnit(player = player)
        val otherBattleUnit = BattleUnitMother.battleUnit(player = player)
        // When
        val result = battleUnit.isSamePlayer(otherBattleUnit)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `IsSamePlayer should be false when the battle units belong to different players`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(player = PlayerMother.player(id = "player-1").toDto())
        val otherBattleUnit = BattleUnitMother.battleUnit(player = PlayerMother.player(id = "player-2").toDto())
        // When
        val result = battleUnit.isSamePlayer(otherBattleUnit)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanCastAbility should be true when the battle unit has actions, no cooldown and enough mana`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", cost = 3, cooldown = 5).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `CanCastAbility should be false when the ability is on cooldown`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", cost = 3, cooldown = 5).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
                .castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanCastAbility should be false when the battle unit has no remaining casts`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", cost = 3, cooldown = 5).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1", "ability-2"), manaPoints = 10).toDto())
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanCastAbility should be false when the ability is not in the battle unit`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-2", cost = 3, cooldown = 5).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanCastAbility should not be able to cast when the battle unit has no enough mana`() {
        // Given
        val ability = AbilityMother.ability(cost = 3).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf(ability.id), manaPoints = 0).toDto())
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CastAbility should reduce the mana and casts, set the cooldown and publish the event when the ability is cast`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
        // When
        val castGroup = listOf(Battlefield.Dto.PositionDto(1, 1))
        val updatedBattleUnit = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityCost = 3, castGroup = castGroup)
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.remainingManaPoints shouldBe 7
        battleUnitDto.remainingTurnActions.remainingCasts shouldBe 0
        battleUnitDto.abilityCooldowns.shouldContain("ability-1", 2)
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.AbilityCasted(battleUnitId = battleUnitDto.id, abilityId = "ability-1", castGroup = castGroup))
    }

    @Test
    fun `ReceiveDelayedEffect should add the on turn started effect and publish the received event when the battle unit receives it`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        val effectId = "effect-1"
        // When
        val updatedBattleUnit = battleUnit.receiveOnTurnStartedEffect(effectId = effectId, turnsLeft = 2)
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onTurnStarted
            .shouldContainExactly(effectId)
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.EffectReceived(battleUnitId = updatedBattleUnit.toDto().id, effectId = effectId))
    }

    @Test
    fun `ReceiveOnDefeatedEffect should add the on defeated effect and publish the received event when the battle unit receives it`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        val effectId = "effect-1"
        // When
        val updatedBattleUnit = battleUnit.receiveOnDefeatedEffect(effectId = effectId)
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onDefeatedEffects
            .shouldContainExactly(effectId)
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.EffectReceived(battleUnitId = updatedBattleUnit.toDto().id, effectId = effectId))
    }

    @Test
    fun `IsDefeated should be false when the battle unit still has health points`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(healthPoints = 10).toDto())
        // When
        val result = battleUnit.isDefeated()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `IsDefeated should be true when the battle unit has no health points left`() {
        // Given
        val unit = UnitMother.unit(healthPoints = 1).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .applyImmediateEffect(
                    effect = EffectMother.decreaseHealthEffect(damage = 1).toDto(),
                    currentRow = position().row,
                    currentColumn = position().column,
                )
        // When
        val result = battleUnit.isDefeated()
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `ApplyOnDefeatedEffects should remove the on defeated effects when the on defeated effects are applied`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit().receiveOnDefeatedEffect(effectId = "effect-1")
        // When
        val updatedBattleUnit = battleUnit.applyOnDefeatedEffects()
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onDefeatedEffects
            .shouldBeEmpty()
    }

    @Test
    fun `ApplyOnDefeatedEffects should keep the on turn started effects when the on defeated effects are applied`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit()
                .receiveOnDefeatedEffect(effectId = "effect-1")
                .receiveOnTurnStartedEffect(effectId = "effect-2", turnsLeft = 2)
        // When
        val updatedBattleUnit = battleUnit.applyOnDefeatedEffects()
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onDefeatedEffects
            .shouldBeEmpty()
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onTurnStarted
            .shouldContainExactly("effect-2")
    }

    @Test
    fun `ApplyImmediateEffect should reduce the health points and publish the damaged event when the effect decreases health`() {
        // Given
        val unit = UnitMother.unit(healthPoints = 10).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit)
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = EffectMother.decreaseHealthEffect(damage = 3).toDto(),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 7
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `ApplyImmediateEffect should publish the defeated event when the battle unit health points reach zero`() {
        // Given
        val unit = UnitMother.unit(healthPoints = 3).toDto()
        val player = PlayerMother.player(id = "player-1").toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit, player = player)
        val defeatedAtPosition = position()
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = EffectMother.decreaseHealthEffect(damage = 3).toDto(),
                currentRow = defeatedAtPosition.row,
                currentColumn = defeatedAtPosition.column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 0
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(
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
    fun `ApplyImmediateEffect should increase the health points up to the unit maximum and publish the healed event when the effect increases health`() {
        // Given
        val unit = UnitMother.unit(healthPoints = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .applyImmediateEffect(
                    effect = EffectMother.decreaseHealthEffect(damage = 4).toDto(),
                    currentRow = position().row,
                    currentColumn = position().column,
                ).pullEvents()
                .second
        val healingEffect = EffectMother.increaseHealthEffect(healing = 20).toDto()
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = healingEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 10
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitHealed(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `ApplyImmediateEffect should increase the mana points and publish the mana restored event when the effect increases mana`() {
        // Given
        val unit = UnitMother.unit(manaPoints = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 7, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        val manaEffect = EffectMother.increaseManaEffect(mana = 5).toDto()
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = manaEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingManaPoints shouldBe 8
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitManaRestored(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `ApplyImmediateEffect should increase the mana points up to the unit maximum when the effect restores more mana than spent`() {
        // Given
        val unit = UnitMother.unit(manaPoints = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityCost = 5, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        val manaEffect = EffectMother.increaseManaEffect(mana = 20).toDto()
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = manaEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingManaPoints shouldBe 10
    }

    @Test
    fun `ApplyImmediateEffect should publish the teleported event when the effect teleports the battle unit`() {
        // Given
        val unit = UnitMother.unit(healthPoints = 10).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit)
        val teleportEffect = EffectMother.teleportEffect().toDto()
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = teleportEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitTeleported(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `HasDelayedOngoingEffects should be true when the battle unit has on turn started effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit().receiveOnTurnStartedEffect(effectId = "effect-1", turnsLeft = 2)
        // When
        val result = battleUnit.hasOnTurnStartedEffects()
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `HasDelayedOngoingEffects should be false when the battle unit has no on turn started effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val result = battleUnit.hasOnTurnStartedEffects()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `HasDelayedOngoingEffects should be false when the battle unit only has on defeated effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit().receiveOnDefeatedEffect(effectId = "effect-1")
        // When
        val result = battleUnit.hasOnTurnStartedEffects()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `ApplyDelayedEffect should reduce the health points and remove the on turn started effect when the effect decreases health`() {
        // Given
        val unit = UnitMother.unit(healthPoints = 10).toDto()
        val onTurnStartedEffect = EffectMother.decreaseHealthEffect(damage = 3, applicationType = "ON_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnTurnStartedEffect(effectId = onTurnStartedEffect.id, turnsLeft = 1)
                .pullEvents()
                .second
        // When
        val updatedBattleUnit =
            battleUnit.applyOnTurnStartedEffect(
                effect = onTurnStartedEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 7
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onTurnStarted
            .shouldBeEmpty()
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.OnTurnStartedEffectApplied(battleUnitId = updatedBattleUnit.toDto().id, effectId = onTurnStartedEffect.id),
            BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id),
        )
    }

    @Test
    fun `ApplyDelayedEffect should publish the applied event before the defeat when the effect defeats the battle unit`() {
        // Given
        val unit = UnitMother.unit(healthPoints = 3).toDto()
        val onTurnStartedEffect = EffectMother.decreaseHealthEffect(damage = 3, applicationType = "ON_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnTurnStartedEffect(effectId = onTurnStartedEffect.id, turnsLeft = 1)
                .pullEvents()
                .second
        // When
        val updatedBattleUnit =
            battleUnit.applyOnTurnStartedEffect(
                effect = onTurnStartedEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        val (events, _) = updatedBattleUnit.pullEvents()
        events.map { event -> event::class }.shouldContainExactly(
            BattleUnitEvent.OnTurnStartedEffectApplied::class,
            BattleUnitEvent.BattleUnitDamaged::class,
            BattleUnitEvent.BattleUnitDefeated::class,
        )
    }

    @Test
    fun `ApplyDelayedEffect should keep the on turn started effect with one turn less when the effect has more turns left`() {
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
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 7
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onTurnStarted
            .shouldContainExactly("effect-1")
    }

    @Test
    fun `ApplyDelayedEffect should remove the on turn started effect when it is applied for its last turn`() {
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
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onTurnStarted
            .shouldBeEmpty()
    }

    @Test
    fun `ApplyDelayedEffect should only update the matching on turn started effect when the battle unit has several effects`() {
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
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 7
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onTurnStarted
            .shouldContainExactly("effect-1", "effect-2")
    }

    @Test
    fun `ApplyDelayedEffect should keep the on turn started effect when an immediate effect with the same id is applied`() {
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
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 7
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onTurnStarted
            .shouldContainExactly("effect-1")
    }

    @Test
    fun `ApplyEquippedEffect should raise the maximums without changing the remaining health and mana when the item effects are applied`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(healthPoints = 100, manaPoints = 30).toDto(), remainingHealthPoints = 80)
        val maximumHealthEffect = EffectMother.increaseMaximumHealthEffect(id = "maximum-health-20", healthPoints = 20).toDto()
        val maximumManaEffect = EffectMother.increaseMaximumManaEffect(id = "maximum-mana-20", manaPoints = 20).toDto()
        // When
        val updatedBattleUnit =
            battleUnit
                .applyEquippedEffect(lootableItemId = "hammer", effect = maximumHealthEffect)
                .applyEquippedEffect(lootableItemId = "hammer", effect = maximumManaEffect)
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.maximumHealthPoints shouldBe 120
        battleUnitDto.maximumManaPoints shouldBe 50
        battleUnitDto.remainingHealthPoints shouldBe 80
        battleUnitDto.remainingManaPoints shouldBe 30
        battleUnitDto.equippedEffects shouldBe mapOf("hammer" to listOf("maximum-health-20", "maximum-mana-20"))
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.EffectReceived(battleUnitId = battleUnitDto.id, effectId = "maximum-health-20"),
            BattleUnitEvent.EffectReceived(battleUnitId = battleUnitDto.id, effectId = "maximum-mana-20"),
        )
    }

    @Test
    fun `ApplyEquippedEffect should fail when the item already applied the effect`() {
        // Given
        val maximumHealthEffect = EffectMother.increaseMaximumHealthEffect().toDto()
        val battleUnit = BattleUnitMother.battleUnit().applyEquippedEffect(lootableItemId = "hammer", effect = maximumHealthEffect)
        // When
        val result = runCatching { battleUnit.applyEquippedEffect(lootableItemId = "hammer", effect = maximumHealthEffect) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.EquippedEffectAlreadyApplied>()
    }

    @Test
    fun `ApplyEquippedEffect should fail when the effect does not last while equipped`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val result = runCatching { battleUnit.applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseHealthEffect().toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.InvalidEffectApplicationStatus>()
    }

    @Test
    fun `ApplyImmediateEffect should increase the health points up to the maximum with the equipped effects when the effect increases health`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(healthPoints = 100).toDto(), remainingHealthPoints = 90)
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(healthPoints = 20).toDto())
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = EffectMother.increaseHealthEffect(healing = 50).toDto(),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealthPoints shouldBe 120
    }

    @Test
    fun `ApplyImmediateEffect should increase the mana points up to the maximum with the equipped effects when the effect increases mana`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(manaPoints = 30).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumManaEffect(manaPoints = 20).toDto())
        // When
        val updatedBattleUnit =
            battleUnit.applyImmediateEffect(
                effect = EffectMother.increaseManaEffect(mana = 50).toDto(),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingManaPoints shouldBe 50
    }

    @Test
    fun `ReplenishMana should replenish the remaining mana points up to the maximum with the equipped effects when mana is replenished`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(manaPoints = 30).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumManaEffect(manaPoints = 5).toDto())
        // When
        val replenishedBattleUnit = battleUnit.replenishMana()
        // Then
        replenishedBattleUnit.toDto().remainingManaPoints shouldBe 35
    }

    @Test
    fun `RemoveEquippedEffects should lower the maximums and the remaining health and mana above them when the item effects are removed`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(healthPoints = 100, manaPoints = 30).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(healthPoints = 20).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumManaEffect(manaPoints = 20).toDto())
                .applyImmediateEffect(effect = EffectMother.increaseHealthEffect(healing = 20).toDto(), currentRow = 0, currentColumn = 0)
                .applyImmediateEffect(effect = EffectMother.increaseManaEffect(mana = 20).toDto(), currentRow = 0, currentColumn = 0)
                .pullEvents()
                .second
        require(battleUnit.toDto().remainingHealthPoints == 120 && battleUnit.toDto().remainingManaPoints == 50)
        // When
        val updatedBattleUnit = battleUnit.removeEquippedEffects(lootableItemId = "hammer")
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.maximumHealthPoints shouldBe 100
        battleUnitDto.maximumManaPoints shouldBe 30
        battleUnitDto.remainingHealthPoints shouldBe 100
        battleUnitDto.remainingManaPoints shouldBe 30
        battleUnitDto.equippedEffects shouldBe emptyMap()
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.EquippedEffectsRemoved(battleUnitId = battleUnitDto.id, lootableItemId = "hammer"))
    }

    @Test
    fun `RemoveEquippedEffects should keep the remaining health and mana when they are within the new maximums`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(healthPoints = 100, manaPoints = 30).toDto(), remainingHealthPoints = 70)
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(healthPoints = 20).toDto())
                .applyEquippedEffect(lootableItemId = "helmet", effect = EffectMother.increaseMaximumManaEffect(manaPoints = 5).toDto())
        // When
        val updatedBattleUnit = battleUnit.removeEquippedEffects(lootableItemId = "hammer")
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.remainingHealthPoints shouldBe 70
        battleUnitDto.remainingManaPoints shouldBe 30
        battleUnitDto.maximumManaPoints shouldBe 35
        battleUnitDto.equippedEffects.keys.shouldContainExactly("helmet")
    }

    @Test
    fun `RemoveEquippedEffects should fail when the item applied no effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val result = runCatching { battleUnit.removeEquippedEffects(lootableItemId = "hammer") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.EquippedEffectsNotFound>()
    }
}
