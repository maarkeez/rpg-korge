package com.mkz.rpg.screen

import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AbilityCastPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.AppliedEffectPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.TargetPreview
import com.mkz.rpg.battleUnit.usecases.queries.PreviewAbilityCast.TriggeredPreview

/**
 * Turns an [AbilityCastPreview] into the lines of the preview sheet, e.g. `Rat: 20 -> 10 HP, +Venom damage ×5`.
 * Effects that can't be previewed always show as a `?` line, they are never hidden.
 */
class CastPreviewSummary(
    private val unitNameOf: (battleUnitId: String) -> String?,
) {
    data class Line(
        val text: String,
        val kind: Kind,
    ) {
        enum class Kind { TARGET, LETHAL_TARGET, TILE, CONDITIONAL, UNSUPPORTED }
    }

    operator fun invoke(preview: AbilityCastPreview): List<Line> =
        preview.targets.map(::targetLine) +
            preview.tiles.map { tile ->
                val summoned = tile.deploysUnitId?.let(::readable)
                Line("${if (summoned != null) "Summons $summoned at" else "Tile"} row ${tile.row}, col ${tile.column}", Line.Kind.TILE)
            } +
            preview.triggered.map(::triggeredLine) +
            preview.unsupported.map { description -> Line("? $description", Line.Kind.UNSUPPORTED) }

    private fun triggeredLine(triggered: TriggeredPreview): Line {
        val prefix = if (triggered.condition == TriggeredPreview.Condition.ON_LETHAL_HIT) "On defeat" else "If defeated"
        val count = triggered.affectedBattleUnitIds.size
        return Line("$prefix: spreads ${readable(triggered.effectId)} to $count ${if (count == 1) "ally" else "allies"}", Line.Kind.CONDITIONAL)
    }

    private fun targetLine(target: TargetPreview): Line {
        val name = unitNameOf(target.battleUnitId) ?: "Unit"
        val parts =
            buildList {
                if (target.hpAfter != target.hpBefore) add("${target.hpBefore} -> ${target.hpAfter} HP")
                target.teleportTo?.let { add("teleports to row ${it.row}, col ${it.column}") }
                target.appliedEffects.filter { it.timing != AppliedEffectPreview.Timing.IMMEDIATE }.forEach { add(effectText(it)) }
                if (target.isLethal) add("defeated")
            }
        val text = if (parts.isEmpty()) name else "$name: ${parts.joinToString(", ")}"
        return Line(text, if (target.isLethal) Line.Kind.LETHAL_TARGET else Line.Kind.TARGET)
    }

    private fun effectText(effect: AppliedEffectPreview): String =
        when (effect.timing) {
            AppliedEffectPreview.Timing.OVER_TIME -> "+${readable(effect.effectId)}${effect.turns?.let { " ×$it" } ?: ""}"
            AppliedEffectPreview.Timing.ON_DEATH -> "+${readable(effect.effectId)}"
            AppliedEffectPreview.Timing.IMMEDIATE -> readable(effect.effectId)
        }

    private fun readable(id: String) = id.replace('-', ' ').replaceFirstChar { it.uppercase() }
}
