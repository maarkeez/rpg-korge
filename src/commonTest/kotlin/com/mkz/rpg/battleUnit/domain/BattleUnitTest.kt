package com.mkz.rpg.battleUnit.domain

import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
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
        val unit = UnitMother.unit(baseMaximumHealth = 10, baseMaximumMana = 5, maximumSteps = 3).toDto()
        val player = PlayerMother.player().toDto()
        // When
        val deployedBattleUnit = BattleUnit.deploy(id = battleUnitId, unit = unit, player = player, maximumAbilities = 6, deployAtRow = 2, deployAtColumn = 3)
        // Then
        val battleUnitDto = deployedBattleUnit.toDto()
        battleUnitDto.id shouldBe battleUnitId
        battleUnitDto.unitId shouldBe unit.id
        battleUnitDto.playerId shouldBe player.id
        battleUnitDto.remainingHealth shouldBe 10
        battleUnitDto.remainingMana shouldBe 5
        battleUnitDto.maximumHealth shouldBe 10
        battleUnitDto.maximumMana shouldBe 5
        battleUnitDto.equippedEffects shouldBe emptyMap()
        battleUnitDto.remainingActions shouldBe BattleUnit.Dto.RemainingActionsDto(remainingCasts = 1, remainingSteps = 3, maximumSteps = 3, maximumCasts = 1)
    }

    @Test
    fun `Deploy should publish the battle unit deployed event when the battle unit is deployed`() {
        // Given
        val battleUnitId = "battle-unit-1"
        val unit = UnitMother.unit().toDto()
        val player = PlayerMother.player().toDto()
        // When
        val deployedBattleUnit = BattleUnit.deploy(id = battleUnitId, unit = unit, player = player, maximumAbilities = 6, deployAtRow = 2, deployAtColumn = 3)
        // Then
        val (events, _) = deployedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitDeployed(playerId = player.id, battleUnitId = battleUnitId, row = 2, column = 3),
            BattlefieldEvent.RequestOccupyTile(occupantId = battleUnitId, row = 2, column = 3),
        )
    }

    @Test
    fun `WalkInBattle should reduce the remaining steps and publish the moved event when the battle unit walks`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(maximumSteps = 3).toDto())
        val path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2))
        // When
        val movedBattleUnit = battleUnit.walkInBattle(fromRow = 0, fromColumn = 0, path = path)
        // Then
        movedBattleUnit.toDto().remainingActions.remainingSteps shouldBe 1
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
            BattlefieldEvent.RequestMoveOccupant(occupantId = movedBattleUnit.toDto().id, toRow = 0, toColumn = 2),
        )
    }

    @Test
    fun `WalkInBattle should fail when the walking distance is not greater than zero`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val result = runCatching { battleUnit.walkInBattle(fromRow = 0, fromColumn = 0, path = emptyList()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.WalkingDistanceMustBeGreaterThanZero>()
    }

    @Test
    fun `WalkInBattle should fail when the walking distance exceeds the remaining steps`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(maximumSteps = 3).toDto())
        // When
        val path = (1..4).map { column -> Battlefield.Dto.PositionDto(row = 0, column = column) }
        val result = runCatching { battleUnit.walkInBattle(fromRow = 0, fromColumn = 0, path = path) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.WalkingDistanceExceedsRemainingSteps>()
    }

    @Test
    fun `WalkOutOfBattle should publish the moved event and keep the remaining steps when the battle unit walks out of a battle`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(maximumSteps = 3).toDto())
        val path = (1..5).map { column -> Battlefield.Dto.PositionDto(row = 0, column = column) }
        // When
        val walkedBattleUnit = battleUnit.walkOutOfBattle(fromRow = 0, fromColumn = 0, path = path)
        // Then
        walkedBattleUnit.toDto().remainingActions.remainingSteps shouldBe 3
        val (events, _) = walkedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitMoved(
                battleUnitId = walkedBattleUnit.toDto().id,
                fromRow = 0,
                fromColumn = 0,
                toRow = 0,
                toColumn = 5,
                path = path,
            ),
            BattlefieldEvent.RequestMoveOccupant(occupantId = walkedBattleUnit.toDto().id, toRow = 0, toColumn = 5),
        )
    }

    @Test
    fun `Teleport should publish the moved event and keep the remaining steps when the battle unit is teleported`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(maximumSteps = 3).toDto())
        // When
        val teleportedBattleUnit = battleUnit.teleport(fromRow = 0, fromColumn = 0, toRow = 2, toColumn = 2)
        // Then
        teleportedBattleUnit.toDto().remainingActions.remainingSteps shouldBe 3
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
            BattlefieldEvent.RequestMoveOccupant(occupantId = teleportedBattleUnit.toDto().id, toRow = 2, toColumn = 2),
        )
    }

    @Test
    fun `ResetActions should restore the remaining steps and casts when the actions are reset`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(maximumSteps = 3).toDto())
                .walkInBattle(fromRow = 0, fromColumn = 0, path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2)))
        // When
        val resetBattleUnit = battleUnit.resetActions()
        // Then
        resetBattleUnit.toDto().remainingActions shouldBe BattleUnit.Dto.RemainingActionsDto(remainingCasts = 1, remainingSteps = 3, maximumSteps = 3, maximumCasts = 1)
    }

    @Test
    fun `ReduceCooldowns should reduce the ability cooldowns when the turn passes`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = listOf("ability-1")).toDto())
        val battleUnitWithCooldown = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 3, abilityManaCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val updatedBattleUnit = battleUnitWithCooldown.reduceCooldowns()
        // Then
        updatedBattleUnit.toDto().knownAbilities.shouldContain("ability-1", 2)
    }

    @Test
    fun `ReplenishMana should replenish the remaining mana up to the unit maximum when mana is replenished`() {
        // Given
        val unit = UnitMother.unit(baseMaximumMana = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityManaCost = 5, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val replenishedBattleUnit = battleUnit.replenishMana()
        // Then
        replenishedBattleUnit.toDto().remainingMana shouldBe 10
    }

    @Test
    fun `CanWalkDistance should be true when the distance is within the remaining steps`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(maximumSteps = 3).toDto())
        // When
        val result = battleUnit.canWalkDistance(distance = 2)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `CanWalkDistance should be false when the distance exceeds the remaining steps`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(maximumSteps = 3).toDto())
        // When
        val result = battleUnit.canWalkDistance(distance = 4)
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
        val ability = AbilityMother.ability(id = "ability-1", manaCost = 3, cooldown = 5).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = listOf("ability-1"), baseMaximumMana = 10).toDto())
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `CanCastAbility should be false when the ability is on cooldown`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", manaCost = 3, cooldown = 5).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(startingAbilities = listOf("ability-1"), baseMaximumMana = 10).toDto())
                .castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityManaCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanCastAbility should be false when the battle unit has no remaining casts`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", manaCost = 3, cooldown = 5).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(startingAbilities = listOf("ability-1", "ability-2"), baseMaximumMana = 10).toDto())
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityManaCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanCastAbility should be false when the ability is not in the battle unit`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-2", manaCost = 3, cooldown = 5).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = listOf("ability-1"), baseMaximumMana = 10).toDto())
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanCastAbility should not be able to cast when the battle unit has no enough mana`() {
        // Given
        val ability = AbilityMother.ability(manaCost = 3).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = listOf(ability.id), baseMaximumMana = 0).toDto())
        // When
        val result = battleUnit.canCastAbility(ability = ability)
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanLearnAbility should be true when the battle unit does not know the ability and knows fewer than its maximum`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = List(2) { index -> "ability-$index" }).toDto(), maximumAbilities = 3)
        // When
        val result = battleUnit.canLearnAbility(abilityId = "teleport")
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `CanLearnAbility should be false when the battle unit already knows the ability`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = listOf("sword", "teleport")).toDto())
        // When
        val result = battleUnit.canLearnAbility(abilityId = "teleport")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CanLearnAbility should be false when the battle unit already knows its maximum abilities`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = List(3) { index -> "ability-$index" }).toDto(), maximumAbilities = 3)
        // When
        val result = battleUnit.canLearnAbility(abilityId = "teleport")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `CastAbility should reduce the mana and casts, set the cooldown and publish the event when the ability is cast`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = listOf("ability-1"), baseMaximumMana = 10).toDto())
        // When
        val castGroup = listOf(Battlefield.Dto.PositionDto(1, 1))
        val updatedBattleUnit = battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityManaCost = 3, castGroup = castGroup)
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.remainingMana shouldBe 7
        battleUnitDto.remainingActions.remainingCasts shouldBe 0
        battleUnitDto.knownAbilities.shouldContain("ability-1", 2)
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.AbilityCasted(battleUnitId = battleUnitDto.id, abilityId = "ability-1", castGroup = castGroup))
    }

    @Test
    fun `ReceiveOnPlayerTurnStartedEffect should add the on player turn started effect and publish the received event when the battle unit receives it`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        val effectId = "effect-1"
        // When
        val updatedBattleUnit = battleUnit.receiveOnPlayerTurnStartedEffect(effectId = effectId, remainingDuration = 2)
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onPlayerTurnStarted
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
    fun `IsDefeated should be false when the battle unit still has health`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(baseMaximumHealth = 10).toDto())
        // When
        val result = battleUnit.isDefeated()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `IsDefeated should be true when the battle unit has no health left`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 1).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .triggerImmediateEffect(
                    effect = EffectMother.damageEffect(damage = 1).toDto(),
                    currentRow = position().row,
                    currentColumn = position().column,
                )
        // When
        val result = battleUnit.isDefeated()
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `TriggerOnDefeatedEffects should remove the on defeated effects when the on defeated effects are triggered`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit().receiveOnDefeatedEffect(effectId = "effect-1")
        // When
        val updatedBattleUnit = battleUnit.triggerOnDefeatedEffects()
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onDefeatedEffects
            .shouldBeEmpty()
    }

    @Test
    fun `TriggerOnDefeatedEffects should keep the on player turn started effects when the on defeated effects are triggered`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit()
                .receiveOnDefeatedEffect(effectId = "effect-1")
                .receiveOnPlayerTurnStartedEffect(effectId = "effect-2", remainingDuration = 2)
        // When
        val updatedBattleUnit = battleUnit.triggerOnDefeatedEffects()
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onDefeatedEffects
            .shouldBeEmpty()
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onPlayerTurnStarted
            .shouldContainExactly("effect-2")
    }

    @Test
    fun `TriggerImmediateEffect should reduce the health and publish the damaged event when the effect damages`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit)
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = EffectMother.damageEffect(damage = 3).toDto(),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 7
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `TriggerImmediateEffect should publish the defeated event and request its loot drop when the battle unit health reaches zero`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 3).toDto()
        val player = PlayerMother.player(id = "player-1").toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit, player = player)
        val defeatedAtPosition = position()
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = EffectMother.damageEffect(damage = 3).toDto(),
                currentRow = defeatedAtPosition.row,
                currentColumn = defeatedAtPosition.column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 0
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id),
            BattleUnitEvent.BattleUnitDefeated(
                playerId = player.id,
                battleUnitId = updatedBattleUnit.toDto().id,
                unitId = unit.id,
                defeatedAtRow = defeatedAtPosition.row,
                defeatedAtColumn = defeatedAtPosition.column,
            ),
            BattlefieldEvent.RequestRemoveOccupant(occupantId = updatedBattleUnit.toDto().id),
        )
    }

    @Test
    fun `TriggerImmediateEffect should increase the health up to the unit maximum and publish the healed event when the effect heals`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .triggerImmediateEffect(
                    effect = EffectMother.damageEffect(damage = 4).toDto(),
                    currentRow = position().row,
                    currentColumn = position().column,
                ).pullEvents()
                .second
        val healingEffect = EffectMother.healEffect(healing = 20).toDto()
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = healingEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 10
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitHealed(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `TriggerImmediateEffect should increase the mana and publish the mana restored event when the effect restores mana`() {
        // Given
        val unit = UnitMother.unit(baseMaximumMana = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityManaCost = 7, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        val manaEffect = EffectMother.restoreManaEffect(mana = 5).toDto()
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = manaEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingMana shouldBe 8
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitManaRestored(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `TriggerImmediateEffect should increase the mana up to the unit maximum when the effect gives more mana than spent`() {
        // Given
        val unit = UnitMother.unit(baseMaximumMana = 10).toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityManaCost = 5, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
        val manaEffect = EffectMother.restoreManaEffect(mana = 20).toDto()
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = manaEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingMana shouldBe 10
    }

    @Test
    fun `TriggerImmediateEffect should publish the teleported event when the effect teleports the battle unit`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit)
        val teleportEffect = EffectMother.teleportEffect().toDto()
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = teleportEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.BattleUnitTeleported(battleUnitId = updatedBattleUnit.toDto().id))
    }

    @Test
    fun `TriggerImmediateEffect should learn the ability after the known ones, ready to be cast, when the effect teaches an ability`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(startingAbilities = listOf("sword")).toDto())
                .castAbility(abilityId = "sword", abilityCooldown = 2, abilityManaCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .pullEvents()
                .second
        val learnTeleportEffect = EffectMother.learnAbilityEffect(abilityId = "teleport").toDto()
        // When
        val (events, updatedBattleUnit) =
            battleUnit.triggerImmediateEffect(effect = learnTeleportEffect, currentRow = 0, currentColumn = 0).pullEvents()
        // Then
        updatedBattleUnit
            .toDto()
            .knownAbilities
            .toList()
            .shouldContainExactly("sword" to 2, "teleport" to 0)
        events.shouldContainExactly(BattleUnitEvent.AbilityLearned(battleUnitId = updatedBattleUnit.toDto().id, abilityId = "teleport"))
    }

    @Test
    fun `TriggerImmediateEffect should fail when the effect teaches an ability the battle unit already knows`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = listOf("sword", "teleport")).toDto())
        val learnTeleportEffect = EffectMother.learnAbilityEffect(abilityId = "teleport").toDto()
        // When
        val result = runCatching { battleUnit.triggerImmediateEffect(effect = learnTeleportEffect, currentRow = 0, currentColumn = 0) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.AbilityAlreadyKnown>()
    }

    @Test
    fun `TriggerImmediateEffect should fail when the effect teaches an ability and the battle unit already knows its maximum`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(startingAbilities = List(3) { index -> "ability-$index" }).toDto(), maximumAbilities = 3)
        val learnTeleportEffect = EffectMother.learnAbilityEffect(abilityId = "teleport").toDto()
        // When
        val result = runCatching { battleUnit.triggerImmediateEffect(effect = learnTeleportEffect, currentRow = 0, currentColumn = 0) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.AbilitiesAboveLimit>()
    }

    @Test
    fun `HasOnPlayerTurnStartedEffects should be true when the battle unit has on player turn started effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit().receiveOnPlayerTurnStartedEffect(effectId = "effect-1", remainingDuration = 2)
        // When
        val result = battleUnit.hasOnPlayerTurnStartedEffects()
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `HasOnPlayerTurnStartedEffects should be false when the battle unit has no on player turn started effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val result = battleUnit.hasOnPlayerTurnStartedEffects()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `HasOnPlayerTurnStartedEffects should be false when the battle unit only has on defeated effects`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit().receiveOnDefeatedEffect(effectId = "effect-1")
        // When
        val result = battleUnit.hasOnPlayerTurnStartedEffects()
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should reduce the health and remove the on player turn started effect when the effect damages`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val onPlayerTurnStartedEffect = EffectMother.damageEffect(damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnPlayerTurnStartedEffect(effectId = onPlayerTurnStartedEffect.id, remainingDuration = 1)
                .pullEvents()
                .second
        // When
        val updatedBattleUnit =
            battleUnit.triggerOnPlayerTurnStartedEffects(
                effects = listOf(onPlayerTurnStartedEffect),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 7
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onPlayerTurnStarted
            .shouldBeEmpty()
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(
            BattleUnitEvent.OnPlayerTurnStartedEffectTriggered(battleUnitId = updatedBattleUnit.toDto().id, effectId = onPlayerTurnStartedEffect.id),
            BattleUnitEvent.BattleUnitDamaged(battleUnitId = updatedBattleUnit.toDto().id),
        )
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should publish the triggered event before the defeat when the effect defeats the battle unit`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 3).toDto()
        val onPlayerTurnStartedEffect = EffectMother.damageEffect(damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnPlayerTurnStartedEffect(effectId = onPlayerTurnStartedEffect.id, remainingDuration = 1)
                .pullEvents()
                .second
        // When
        val updatedBattleUnit =
            battleUnit.triggerOnPlayerTurnStartedEffects(
                effects = listOf(onPlayerTurnStartedEffect),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        val (events, _) = updatedBattleUnit.pullEvents()
        events.map { event -> event::class }.shouldContainExactly(
            BattleUnitEvent.OnPlayerTurnStartedEffectTriggered::class,
            BattleUnitEvent.BattleUnitDamaged::class,
            BattleUnitEvent.BattleUnitDefeated::class,
            BattlefieldEvent.RequestRemoveOccupant::class,
        )
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should keep the on player turn started effect with one less remaining duration when it has more than one left`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val onPlayerTurnStartedEffect = EffectMother.damageEffect(id = "effect-1", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit).receiveOnPlayerTurnStartedEffect(effectId = onPlayerTurnStartedEffect.id, remainingDuration = 2)
        // When
        val updatedBattleUnit =
            battleUnit.triggerOnPlayerTurnStartedEffects(
                effects = listOf(onPlayerTurnStartedEffect),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 7
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onPlayerTurnStarted
            .shouldContainExactly("effect-1")
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should remove the on player turn started effect when it is triggered for its last player turn`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val onPlayerTurnStartedEffect = EffectMother.damageEffect(id = "effect-1", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit).receiveOnPlayerTurnStartedEffect(effectId = onPlayerTurnStartedEffect.id, remainingDuration = 2)
        // When
        val updatedBattleUnit =
            battleUnit
                .triggerOnPlayerTurnStartedEffects(
                    effects = listOf(onPlayerTurnStartedEffect),
                    currentRow = position().row,
                    currentColumn = position().column,
                ).triggerOnPlayerTurnStartedEffects(
                    effects = listOf(onPlayerTurnStartedEffect),
                    currentRow = position().row,
                    currentColumn = position().column,
                )
        // Then
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onPlayerTurnStarted
            .shouldBeEmpty()
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should trigger every on player turn started effect when the battle unit has several effects`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val firstEffect = EffectMother.damageEffect(id = "effect-1", damage = 2, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val secondEffect = EffectMother.damageEffect(id = "effect-2", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnPlayerTurnStartedEffect(effectId = firstEffect.id, remainingDuration = 2)
                .receiveOnPlayerTurnStartedEffect(effectId = secondEffect.id, remainingDuration = 2)
        // When
        val updatedBattleUnit =
            battleUnit.triggerOnPlayerTurnStartedEffects(
                effects = listOf(firstEffect, secondEffect),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 5
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onPlayerTurnStarted
            .shouldContainExactly("effect-1", "effect-2")
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should deal the damage of every stack when the battle unit has two of the same effect`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val venom = EffectMother.damageEffect(id = "venom-damage", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnPlayerTurnStartedEffect(effectId = venom.id, remainingDuration = 5)
                .receiveOnPlayerTurnStartedEffect(effectId = venom.id, remainingDuration = 5)
                .pullEvents()
                .second
        // When
        val updatedBattleUnit =
            battleUnit.triggerOnPlayerTurnStartedEffects(
                effects = listOf(venom),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 4
        updatedBattleUnit
            .toSnapshot()
            .ongoingEffects
            .map { effect -> effect.remainingDuration }
            .shouldContainExactly(4, 4)
        val (events, _) = updatedBattleUnit.pullEvents()
        events.map { event -> event::class }.shouldContainExactly(
            BattleUnitEvent.OnPlayerTurnStartedEffectTriggered::class,
            BattleUnitEvent.BattleUnitDamaged::class,
        )
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should publish one defeated event and stop triggering when a stack defeats the battle unit`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 3).toDto()
        val venom = EffectMother.damageEffect(id = "venom-damage", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnPlayerTurnStartedEffect(effectId = venom.id, remainingDuration = 5)
                .receiveOnPlayerTurnStartedEffect(effectId = venom.id, remainingDuration = 5)
                .pullEvents()
                .second
        // When
        val updatedBattleUnit =
            battleUnit.triggerOnPlayerTurnStartedEffects(
                effects = listOf(venom),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 0
        updatedBattleUnit
            .toSnapshot()
            .ongoingEffects
            .map { effect -> effect.remainingDuration }
            .shouldContainExactly(4, 5)
        val (events, _) = updatedBattleUnit.pullEvents()
        events.map { event -> event::class }.shouldContainExactly(
            BattleUnitEvent.OnPlayerTurnStartedEffectTriggered::class,
            BattleUnitEvent.BattleUnitDamaged::class,
            BattleUnitEvent.BattleUnitDefeated::class,
            BattlefieldEvent.RequestRemoveOccupant::class,
        )
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should lower the remaining duration of each stack on its own when the stacks were received on different player turns`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 20).toDto()
        val venom = EffectMother.damageEffect(id = "venom-damage", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnPlayerTurnStartedEffect(effectId = venom.id, remainingDuration = 1)
                .receiveOnPlayerTurnStartedEffect(effectId = venom.id, remainingDuration = 5)
                .receiveOnPlayerTurnStartedEffect(effectId = venom.id, remainingDuration = 2)
        // When
        val updatedBattleUnit =
            battleUnit.triggerOnPlayerTurnStartedEffects(
                effects = listOf(venom),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 11
        updatedBattleUnit
            .toSnapshot()
            .ongoingEffects
            .map { effect -> effect.remainingDuration }
            .shouldContainExactly(4, 1)
    }

    @Test
    fun `TriggerImmediateEffect should neither damage nor publish the defeated event when the battle unit is already defeated`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val damageEffect = EffectMother.damageEffect(id = "low-physical-damage", damage = 10, timingType = "IMMEDIATELY").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit, remainingHealth = 0)
                .receiveImmediateEffect(effectId = damageEffect.id)
                .pullEvents()
                .second
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = damageEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 0
        updatedBattleUnit.toSnapshot().ongoingEffects.shouldBeEmpty()
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldBeEmpty()
    }

    @Test
    fun `TriggerOnPlayerTurnStartedEffects should keep the on player turn started effect when an immediate effect with the same id is applied`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 10).toDto()
        val immediateEffect = EffectMother.damageEffect(id = "effect-1", damage = 3, timingType = "IMMEDIATELY").toDto()
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .receiveOnPlayerTurnStartedEffect(effectId = immediateEffect.id, remainingDuration = 2)
                .receiveImmediateEffect(effectId = immediateEffect.id)
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = immediateEffect,
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 7
        updatedBattleUnit
            .toDto()
            .ongoingEffects.onPlayerTurnStarted
            .shouldContainExactly("effect-1")
    }

    @Test
    fun `ApplyEquippedEffect should raise the maximums without changing the remaining health and mana when the item effects are applied`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit(unit = UnitMother.unit(baseMaximumHealth = 100, baseMaximumMana = 30).toDto(), remainingHealth = 80)
        val maximumHealthEffect = EffectMother.increaseMaximumHealthEffect(id = "maximum-health-20", maximumHealth = 20).toDto()
        val maximumManaEffect = EffectMother.increaseMaximumManaEffect(id = "maximum-mana-20", maximumMana = 20).toDto()
        // When
        val updatedBattleUnit =
            battleUnit
                .applyEquippedEffect(lootableItemId = "hammer", effect = maximumHealthEffect)
                .applyEquippedEffect(lootableItemId = "hammer", effect = maximumManaEffect)
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.maximumHealth shouldBe 120
        battleUnitDto.maximumMana shouldBe 50
        battleUnitDto.remainingHealth shouldBe 80
        battleUnitDto.remainingMana shouldBe 30
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
        val result = runCatching { battleUnit.applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.healEffect().toDto()) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattleUnitError.InvalidEffectTiming>()
    }

    @Test
    fun `TriggerImmediateEffect should increase the health up to the maximum with the equipped effects when the effect heals`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(baseMaximumHealth = 100).toDto(), remainingHealth = 90)
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(maximumHealth = 20).toDto())
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = EffectMother.healEffect(healing = 50).toDto(),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingHealth shouldBe 120
    }

    @Test
    fun `TriggerImmediateEffect should increase the mana up to the maximum with the equipped effects when the effect restores mana`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(baseMaximumMana = 30).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumManaEffect(maximumMana = 20).toDto())
        // When
        val updatedBattleUnit =
            battleUnit.triggerImmediateEffect(
                effect = EffectMother.restoreManaEffect(mana = 50).toDto(),
                currentRow = position().row,
                currentColumn = position().column,
            )
        // Then
        updatedBattleUnit.toDto().remainingMana shouldBe 50
    }

    @Test
    fun `ReplenishMana should replenish the remaining mana up to the maximum with the equipped effects when mana is replenished`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(baseMaximumMana = 30).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumManaEffect(maximumMana = 5).toDto())
        // When
        val replenishedBattleUnit = battleUnit.replenishMana()
        // Then
        replenishedBattleUnit.toDto().remainingMana shouldBe 35
    }

    @Test
    fun `RemoveEquippedEffects should lower the maximums and the remaining health and mana above them when the item effects are removed`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(baseMaximumHealth = 100, baseMaximumMana = 30).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(maximumHealth = 20).toDto())
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumManaEffect(maximumMana = 20).toDto())
                .triggerImmediateEffect(effect = EffectMother.healEffect(healing = 20).toDto(), currentRow = 0, currentColumn = 0)
                .triggerImmediateEffect(effect = EffectMother.restoreManaEffect(mana = 20).toDto(), currentRow = 0, currentColumn = 0)
                .pullEvents()
                .second
        require(battleUnit.toDto().remainingHealth == 120 && battleUnit.toDto().remainingMana == 50)
        // When
        val updatedBattleUnit = battleUnit.removeEquippedEffects(lootableItemId = "hammer")
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.maximumHealth shouldBe 100
        battleUnitDto.maximumMana shouldBe 30
        battleUnitDto.remainingHealth shouldBe 100
        battleUnitDto.remainingMana shouldBe 30
        battleUnitDto.equippedEffects shouldBe emptyMap()
        val (events, _) = updatedBattleUnit.pullEvents()
        events.shouldContainExactly(BattleUnitEvent.EquippedEffectsRemoved(battleUnitId = battleUnitDto.id, lootableItemId = "hammer"))
    }

    @Test
    fun `RemoveEquippedEffects should keep the remaining health and mana when they are within the new maximums`() {
        // Given
        val battleUnit =
            BattleUnitMother
                .battleUnit(unit = UnitMother.unit(baseMaximumHealth = 100, baseMaximumMana = 30).toDto(), remainingHealth = 70)
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(maximumHealth = 20).toDto())
                .applyEquippedEffect(lootableItemId = "helmet", effect = EffectMother.increaseMaximumManaEffect(maximumMana = 5).toDto())
        // When
        val updatedBattleUnit = battleUnit.removeEquippedEffects(lootableItemId = "hammer")
        // Then
        val battleUnitDto = updatedBattleUnit.toDto()
        battleUnitDto.remainingHealth shouldBe 70
        battleUnitDto.remainingMana shouldBe 30
        battleUnitDto.maximumMana shouldBe 35
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

    private fun hurtBattleUnit(
        remainingHealth: Int,
        spentMana: Int,
        baseMaximumHealth: Int = 20,
        baseMaximumMana: Int = 10,
    ): BattleUnit {
        val unit = UnitMother.unit(baseMaximumHealth = baseMaximumHealth, baseMaximumMana = baseMaximumMana).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit, remainingHealth = remainingHealth)
        if (spentMana == 0) return battleUnit
        return battleUnit.castAbility(abilityId = "ability-1", abilityCooldown = 0, abilityManaCost = spentMana, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
    }

    @Test
    fun `Recover should recover the percentage of the maximum health and mana`() {
        // Given
        val battleUnit = hurtBattleUnit(remainingHealth = 10, spentMana = 5)
        // When
        val recoveredBattleUnit = battleUnit.recover(percentage = 10)
        // Then
        recoveredBattleUnit.toDto().remainingHealth shouldBe 12
        recoveredBattleUnit.toDto().remainingMana shouldBe 6
    }

    @Test
    fun `Recover should round the recovered health and mana up`() {
        // Given
        val battleUnit = hurtBattleUnit(remainingHealth = 5, spentMana = 5, baseMaximumHealth = 15, baseMaximumMana = 5)
        // When
        val recoveredBattleUnit = battleUnit.recover(percentage = 10)
        // Then
        recoveredBattleUnit.toDto().remainingHealth shouldBe 7
        recoveredBattleUnit.toDto().remainingMana shouldBe 1
    }

    @Test
    fun `Recover should not recover above the maximum health and mana`() {
        // Given
        val battleUnit = hurtBattleUnit(remainingHealth = 19, spentMana = 0)
        // When
        val recoveredBattleUnit = battleUnit.recover(percentage = 10)
        // Then
        recoveredBattleUnit.toDto().remainingHealth shouldBe 20
        recoveredBattleUnit.toDto().remainingMana shouldBe 10
    }

    @Test
    fun `Recover should recover the percentage of the maximums raised by the equipped effects`() {
        // Given
        val battleUnit =
            hurtBattleUnit(remainingHealth = 10, spentMana = 0)
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(maximumHealth = 20).toDto())
        // When
        val recoveredBattleUnit = battleUnit.recover(percentage = 10)
        // Then
        recoveredBattleUnit.toDto().remainingHealth shouldBe 14
    }

    @Test
    fun `Recover should not recover a defeated battle unit`() {
        // Given
        val battleUnit = hurtBattleUnit(remainingHealth = 0, spentMana = 5)
        // When
        val recoveredBattleUnit = battleUnit.recover(percentage = 10)
        // Then
        recoveredBattleUnit.toDto().remainingHealth shouldBe 0
        recoveredBattleUnit.toDto().remainingMana shouldBe 5
        recoveredBattleUnit.isDefeated().shouldBeTrue()
    }

    @Test
    fun `Deploy should deploy a battle unit that never vanishes when it has no remaining summon duration`() {
        // When
        val deployedBattleUnit =
            BattleUnit.deploy(
                id = "battle-unit-1",
                unit = UnitMother.unit().toDto(),
                player = PlayerMother.player().toDto(),
                maximumAbilities = 6,
                deployAtRow = 2,
                deployAtColumn = 3,
            )
        // Then
        deployedBattleUnit.toDto().remainingSummonDuration shouldBe null
    }

    @Test
    fun `Deploy should deploy a summon with its remaining summon duration`() {
        // When
        val deployedBattleUnit =
            BattleUnit.deploy(
                id = "battle-unit-1",
                unit = UnitMother.unit().toDto(),
                player = PlayerMother.player().toDto(),
                maximumAbilities = 6,
                deployAtRow = 2,
                deployAtColumn = 3,
                remainingSummonDuration = 5,
            )
        // Then
        deployedBattleUnit.toDto().remainingSummonDuration shouldBe 5
    }

    @Test
    fun `ReduceRemainingSummonDuration should leave a summon with one player turn fewer when it has more left`() {
        // Given
        val summon = BattleUnitMother.battleUnit(remainingSummonDuration = 5)
        // When
        val (events, reducedBattleUnit) = summon.reduceRemainingSummonDuration(row = 2, column = 3).pullEvents()
        // Then
        reducedBattleUnit.toDto().remainingSummonDuration shouldBe 4
        events.shouldBeEmpty()
    }

    @Test
    fun `ReduceRemainingSummonDuration should vanish a summon on its tile when it finishes its last player turn`() {
        // Given
        val unit = UnitMother.unit().toDto()
        val player = PlayerMother.player().toDto()
        val summon = BattleUnitMother.battleUnit(id = "battle-unit-1", unit = unit, player = player, remainingSummonDuration = 1)
        // When
        val (events, reducedBattleUnit) = summon.reduceRemainingSummonDuration(row = 2, column = 3).pullEvents()
        // Then
        reducedBattleUnit.toDto().remainingSummonDuration shouldBe 0
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitVanished(playerId = player.id, battleUnitId = "battle-unit-1", unitId = unit.id, row = 2, column = 3),
            BattlefieldEvent.RequestRemoveOccupant(occupantId = "battle-unit-1"),
        )
    }

    @Test
    fun `ReduceRemainingSummonDuration should not change a battle unit that never vanishes`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val (events, reducedBattleUnit) = battleUnit.reduceRemainingSummonDuration(row = 2, column = 3).pullEvents()
        // Then
        reducedBattleUnit shouldBe battleUnit
        events.shouldBeEmpty()
    }

    @Test
    fun `ReduceRemainingSummonDuration should not vanish a defeated summon`() {
        // Given
        val summon = BattleUnitMother.battleUnit(remainingHealth = 0, remainingSummonDuration = 1)
        // When
        val (events, reducedBattleUnit) = summon.reduceRemainingSummonDuration(row = 2, column = 3).pullEvents()
        // Then
        reducedBattleUnit.toDto().remainingSummonDuration shouldBe 1
        events.shouldBeEmpty()
    }

    @Test
    fun `Vanish should vanish a summon on its tile, whatever summon duration it has left`() {
        // Given
        val unit = UnitMother.unit().toDto()
        val player = PlayerMother.player().toDto()
        val summon = BattleUnitMother.battleUnit(id = "battle-unit-1", unit = unit, player = player, remainingSummonDuration = 4)
        // When
        val (events, vanishedBattleUnit) = summon.vanish(row = 2, column = 3).pullEvents()
        // Then
        vanishedBattleUnit.toDto().remainingSummonDuration shouldBe 0
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitVanished(playerId = player.id, battleUnitId = "battle-unit-1", unitId = unit.id, row = 2, column = 3),
            BattlefieldEvent.RequestRemoveOccupant(occupantId = "battle-unit-1"),
        )
    }

    @Test
    fun `Vanish should not change a battle unit that never vanishes`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        // When
        val (events, vanishedBattleUnit) = battleUnit.vanish(row = 2, column = 3).pullEvents()
        // Then
        vanishedBattleUnit shouldBe battleUnit
        events.shouldBeEmpty()
    }

    @Test
    fun `Vanish should not vanish a defeated summon`() {
        // Given
        val summon = BattleUnitMother.battleUnit(remainingHealth = 0, remainingSummonDuration = 4)
        // When
        val (events, _) = summon.vanish(row = 2, column = 3).pullEvents()
        // Then
        events.shouldBeEmpty()
    }

    @Test
    fun `Respawn should bring a defeated battle unit back as it was deployed`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 20, baseMaximumMana = 10).toDto()
        val player = PlayerMother.player().toDto()
        val defeatedBattleUnit =
            BattleUnitMother
                .battleUnit(id = "rat-1", unit = unit, player = player)
                .castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityManaCost = 5, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .applyEquippedEffect(lootableItemId = "hammer", effect = EffectMother.increaseMaximumHealthEffect(maximumHealth = 20).toDto())
                .triggerImmediateEffect(EffectMother.damageEffect(damage = 40, timingType = "IMMEDIATELY").toDto(), currentRow = 0, currentColumn = 0)
                .pullEvents()
                .second
        require(defeatedBattleUnit.isDefeated())
        // When
        val (events, respawnedBattleUnit) = defeatedBattleUnit.respawn(unit = unit, row = 2, column = 3).pullEvents()
        // Then
        respawnedBattleUnit.toDto() shouldBe BattleUnitMother.battleUnit(id = "rat-1", unit = unit, player = player).toDto()
        events.shouldContainExactly(
            BattleUnitEvent.BattleUnitRespawned(playerId = player.id, battleUnitId = "rat-1", row = 2, column = 3),
            BattlefieldEvent.RequestOccupyTile(occupantId = "rat-1", row = 2, column = 3),
        )
    }

    @Test
    fun `Respawn should keep the learned abilities after the abilities of its unit, with no cooldown`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 20, startingAbilities = listOf("sword")).toDto()
        val defeatedBattleUnit =
            BattleUnitMother
                .battleUnit(unit = unit)
                .triggerImmediateEffect(EffectMother.learnAbilityEffect(abilityId = "teleport").toDto(), currentRow = 0, currentColumn = 0)
                .castAbility(abilityId = "teleport", abilityCooldown = 2, abilityManaCost = 0, castGroup = listOf(Battlefield.Dto.PositionDto(0, 0)))
                .triggerImmediateEffect(EffectMother.damageEffect(damage = 20, timingType = "IMMEDIATELY").toDto(), currentRow = 0, currentColumn = 0)
                .pullEvents()
                .second
        require(defeatedBattleUnit.isDefeated())
        // When
        val (_, respawnedBattleUnit) = defeatedBattleUnit.respawn(unit = unit, row = 2, column = 3).pullEvents()
        // Then
        respawnedBattleUnit
            .toDto()
            .knownAbilities
            .toList()
            .shouldContainExactly("sword" to 0, "teleport" to 0)
    }

    @Test
    fun `Respawn should keep the maximum abilities of the battle unit`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 20, startingAbilities = listOf("sword")).toDto()
        val defeatedBattleUnit =
            BattleUnitMother
                .battleUnit(unit = unit, maximumAbilities = 3)
                .triggerImmediateEffect(EffectMother.learnAbilityEffect(abilityId = "teleport").toDto(), currentRow = 0, currentColumn = 0)
                .triggerImmediateEffect(EffectMother.damageEffect(damage = 20, timingType = "IMMEDIATELY").toDto(), currentRow = 0, currentColumn = 0)
                .pullEvents()
                .second
        require(defeatedBattleUnit.isDefeated())
        // When
        val (_, respawnedBattleUnit) = defeatedBattleUnit.respawn(unit = unit, row = 2, column = 3).pullEvents()
        // Then
        val withFireball =
            respawnedBattleUnit
                .triggerImmediateEffect(EffectMother.learnAbilityEffect(abilityId = "fireball").toDto(), currentRow = 2, currentColumn = 3)
                .pullEvents()
                .second
        withFireball.canLearnAbility(abilityId = "heal").shouldBeFalse()
    }

    @Test
    fun `Respawn should not change a battle unit that is not defeated`() {
        // Given
        val unit = UnitMother.unit(baseMaximumHealth = 20).toDto()
        val battleUnit = BattleUnitMother.battleUnit(unit = unit, remainingHealth = 5)
        // When
        val (events, respawnedBattleUnit) = battleUnit.respawn(unit = unit, row = 2, column = 3).pullEvents()
        // Then
        respawnedBattleUnit shouldBe battleUnit
        events.shouldBeEmpty()
    }
}
