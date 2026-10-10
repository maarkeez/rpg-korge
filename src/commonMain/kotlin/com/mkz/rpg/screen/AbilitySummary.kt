package com.mkz.rpg.screen

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById

/** Generates the one-line effect summary of an ability, e.g. `3 dmg /turn ×5`. Anything it can't describe reads `?`. */
class AbilitySummary(
    private val searchEffectById: SearchEffectById?,
) {
    operator fun invoke(ability: Ability.Dto): String = ability.effectSpecs.joinToString(", ") { effectSpec -> effectText(effectSpec.effectId, depth = 0) }

    private fun effectText(
        effectId: String,
        depth: Int,
    ): String {
        val effect = searchEffectById?.invoke(effectId) ?: return UNKNOWN
        if (depth > MAX_DEPTH) return UNKNOWN
        val outcome = outcomeText(effect, depth)
        return when (effect.application.type) {
            ApplicationTypeDto.ON_TURN_STARTED -> "$outcome /turn ×${effect.application.onTurnStarted?.duration ?: UNKNOWN}"
            ApplicationTypeDto.ON_DEFEATED -> if (effect.outcome.type == TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES) outcome else "On death: $outcome"
            else -> outcome
        }
    }

    private fun outcomeText(
        effect: Effect.Dto,
        depth: Int,
    ): String =
        when (effect.outcome.type) {
            TypeDto.DECREASE_HEALTH -> "${effect.outcome.decreaseHealth?.damage ?: UNKNOWN} dmg"
            TypeDto.INCREASE_HEALTH -> "+${effect.outcome.increaseHealth?.healing ?: UNKNOWN} HP"
            TypeDto.TELEPORT -> "Teleport"
            TypeDto.DEPLOY_BATTLE_UNIT ->
                "Summon ${effect.outcome.deployBattleUnit
                    ?.unitId
                    ?.let(::readable) ?: UNKNOWN}"
            TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES -> {
                val inner =
                    effect.outcome.applyEffectOnNearbyAllies
                        ?.effectId
                        ?.let { effectText(it, depth + 1) } ?: UNKNOWN
                val prefix = if (effect.application.type == ApplicationTypeDto.ON_DEFEATED) "On death: " else ""
                "${prefix}spreads $inner"
            }
            else -> UNKNOWN
        }

    private fun readable(id: String) = id.replace('-', ' ').replaceFirstChar { it.uppercase() }

    private companion object {
        const val UNKNOWN = "?"
        const val MAX_DEPTH = 3
    }
}
