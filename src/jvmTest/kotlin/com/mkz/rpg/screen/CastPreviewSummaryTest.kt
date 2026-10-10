package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AbilityCastPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AppliedEffectPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AppliedEffectPreview.Timing
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.TargetPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.TilePreview
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.screen.CastPreviewSummary.Line
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CastPreviewSummaryTest {
    private val castPreviewSummary = CastPreviewSummary { battleUnitId -> if (battleUnitId == "rat-a") "Rat" else null }

    @Test
    fun `should describe the health change and the status when the target takes damage over time`() {
        // Given
        val preview =
            preview(
                targets =
                    listOf(
                        target(
                            hpBefore = 20,
                            hpAfter = 10,
                            appliedEffects = listOf(AppliedEffectPreview("venom-damage", Timing.OVER_TIME, perTurn = 3, turns = 5)),
                        ),
                    ),
            )
        // When
        val lines = castPreviewSummary(preview)
        // Then
        assertThat(lines).containsExactly(Line("Rat: 20 -> 10 HP, +Venom damage ×5", Line.Kind.TARGET))
    }

    @Test
    fun `should mark the line as defeated when the target is predicted lethal`() {
        // Given
        val preview = preview(targets = listOf(target(hpBefore = 5, hpAfter = 0, isLethal = true)))
        // When
        val lines = castPreviewSummary(preview)
        // Then
        assertThat(lines).containsExactly(Line("Rat: 5 -> 0 HP, defeated", Line.Kind.LETHAL_TARGET))
    }

    @Test
    fun `should describe the destination when the target is teleported`() {
        // Given
        val preview = preview(targets = listOf(target(teleportTo = PositionDto(row = 3, column = 4))))
        // When
        val lines = castPreviewSummary(preview)
        // Then
        assertThat(lines).containsExactly(Line("Rat: teleports to row 3, col 4", Line.Kind.TARGET))
    }

    @Test
    fun `should describe the summoned unit when the preview deploys a battle unit on a tile`() {
        // Given
        val preview = preview(tiles = listOf(TilePreview(row = 2, column = 5, deploysUnitId = "bee")))
        // When
        val lines = castPreviewSummary(preview)
        // Then
        assertThat(lines).containsExactly(Line("Summons Bee at row 2, col 5", Line.Kind.TILE))
    }

    @Test
    fun `should list the unsupported effects with a question mark when the preview can't describe them`() {
        // Given
        val preview = preview(unsupported = listOf("weird-effect (NEGATE_INCREASE_HEALTH)"))
        // When
        val lines = castPreviewSummary(preview)
        // Then
        assertThat(lines).containsExactly(Line("? weird-effect (NEGATE_INCREASE_HEALTH)", Line.Kind.UNSUPPORTED))
    }

    private fun preview(
        targets: List<TargetPreview> = emptyList(),
        tiles: List<TilePreview> = emptyList(),
        unsupported: List<String> = emptyList(),
    ) = AbilityCastPreview(
        casterId = "knight",
        manaBefore = 30,
        manaAfter = 20,
        cooldownAfter = 0,
        targets = targets,
        tiles = tiles,
        unsupported = unsupported,
    )

    private fun target(
        hpBefore: Int = 20,
        hpAfter: Int = 20,
        isLethal: Boolean = false,
        appliedEffects: List<AppliedEffectPreview> = emptyList(),
        teleportTo: PositionDto? = null,
    ) = TargetPreview(
        battleUnitId = "rat-a",
        hpBefore = hpBefore,
        hpAfter = hpAfter,
        maxHp = 20,
        isLethal = isLethal,
        appliedEffects = appliedEffects,
        teleportTo = teleportTo,
    )
}
