package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AbilityCastPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AppliedEffectPreview.Timing
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/** The preview is only trustworthy while it matches what the real cast does. Each test casts one knight ability for real and compares. */
class AbilityPreviewParityAcceptanceTest {
    private val battle = ShowcaseBattle()

    @Test
    fun `should match the real cast when the poisoned sword is cast`() = assertPreviewMatchesRealCast(abilityId = "poisoned-sword")

    @Test
    fun `should match the real cast when the mushroom is cast`() = assertPreviewMatchesRealCast(abilityId = "mushroom")

    @Test
    fun `should match the real cast when the skull is cast`() = assertPreviewMatchesRealCast(abilityId = "skull")

    @Test
    fun `should match the real cast when the teleport is cast`() = assertPreviewMatchesRealCast(abilityId = "teleport")

    @Test
    fun `should match the real cast when the bee is cast`() = assertPreviewMatchesRealCast(abilityId = "bee")

    @Test
    fun `should match the real cast when the heal is cast`() = assertPreviewMatchesRealCast(abilityId = "heal")

    @Test
    fun `should spread the venom to the neighbour when the previewed skull is lethal and cast for real`() {
        // Given
        battle.eventBus.publish(
            BattleUnitEvent.RequestApplyEffect(
                EffectApplicationDto(
                    source = EffectApplicationDto.ApplicationSourceDto.battleUnit(ShowcaseBattle.KNIGHT),
                    target = EffectTargetDto.Unit(id = RAT_A),
                    effectId = "low-physical-damage",
                ),
            ),
        )
        battle.eventBus.dispatch()
        val castGroup = battle.castGroups("skull").first { group -> group.positions.any { it.row == 6 && it.column == 7 } }
        val preview = battle.battleUnitApi.previewAbilityCast(ShowcaseBattle.KNIGHT, "skull", battle.positionsOf(castGroup))
        // When
        battle.battleUnitApi.castAbility(ShowcaseBattle.KNIGHT, "skull", castGroup)
        battle.eventBus.dispatch()
        // Then
        val triggered = preview.triggered.single()
        assertThat(triggered.condition).isEqualTo(PreviewAbilityCast.TriggeredPreview.Condition.ON_LETHAL_HIT)
        assertThat(triggered.affectedBattleUnitIds).containsExactly(RAT_B)
        val neighbour = battle.battleUnitApi.searchBattleUnitById(RAT_B)!!
        assertThat(neighbour.ongoingEffects.onTurnStarted).contains(triggered.effectId)
        assertThat(battle.battleUnitApi.searchBattleUnitById(RAT_A)!!.remainingHealthPoints).isZero()
    }

    @Test
    fun `should cover every ability of the knight when the parity tests are listed`() {
        // Given / When
        val abilityIds = battle.knightAbilityIds()
        // Then
        assertThat(abilityIds).containsExactlyInAnyOrder("poisoned-sword", "mushroom", "skull", "teleport", "bee", "heal")
    }

    private companion object {
        const val RAT_A = "player-2-unit-1"
        const val RAT_B = "player-2-unit-2"
    }

    private fun assertPreviewMatchesRealCast(abilityId: String) {
        // Given
        val castGroup = battle.castGroups(abilityId).first()
        val preview = battle.battleUnitApi.previewAbilityCast(ShowcaseBattle.KNIGHT, abilityId, battle.positionsOf(castGroup))
        // When
        battle.battleUnitApi.castAbility(ShowcaseBattle.KNIGHT, abilityId, castGroup)
        battle.eventBus.dispatch()
        // Then
        assertThat(preview.targets.size + preview.tiles.size).isGreaterThan(0)
        assertThat(preview.unsupported).isEmpty()
        assertTargetsMatch(preview)
        assertTilesMatch(preview)
        val caster = battle.battleUnitApi.searchBattleUnitById(ShowcaseBattle.KNIGHT)!!
        assertThat(caster.remainingManaPoints).isEqualTo(preview.manaAfter)
        assertThat(caster.abilityCooldowns[abilityId]).isEqualTo(preview.cooldownAfter)
    }

    private fun assertTargetsMatch(preview: AbilityCastPreview) {
        preview.targets.forEach { target ->
            val actual = battle.battleUnitApi.searchBattleUnitById(target.battleUnitId)!!
            assertThat(actual.remainingHealthPoints).describedAs("hp of ${target.battleUnitId}").isEqualTo(target.hpAfter)
            assertThat(actual.remainingHealthPoints <= 0).describedAs("lethal ${target.battleUnitId}").isEqualTo(target.isLethal)
            target.appliedEffects.filter { it.timing == Timing.OVER_TIME }.forEach { effect ->
                assertThat(actual.ongoingEffects.onTurnStarted).contains(effect.effectId)
                assertThat(actual.ongoingEffects.onTurnStartedTurnsLeft[effect.effectId]).isEqualTo(effect.turns)
            }
            target.appliedEffects.filter { it.timing == Timing.ON_DEATH }.forEach { effect ->
                assertThat(actual.ongoingEffects.onDefeatedEffects).contains(effect.effectId)
            }
            target.teleportTo?.let { destination ->
                assertThat(battle.battlefieldApi.searchPosition(target.battleUnitId)).isEqualTo(destination)
            }
        }
    }

    private fun assertTilesMatch(preview: AbilityCastPreview) {
        preview.tiles.forEach { tile ->
            val occupantId = battle.battlefieldApi.searchOccupant(tile.row, tile.column)
            assertThat(occupantId).describedAs("occupant at ${tile.row},${tile.column}").isNotNull()
            assertThat(battle.battleUnitApi.searchBattleUnitById(occupantId!!)!!.unitId).isEqualTo(tile.deploysUnitId)
        }
    }
}
