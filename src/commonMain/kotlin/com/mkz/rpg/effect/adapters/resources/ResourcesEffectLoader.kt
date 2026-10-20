package com.mkz.rpg.effect.adapters.resources

import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.ApplyEffectOnNearbyAlliesDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.DecreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.DeployBattleUnitDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.IncreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.IncreaseManaDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.IncreaseMaximumHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.IncreaseMaximumManaDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.LearnAbilityDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.usecases.services.EffectLoader
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.shared.adapters.toml.required
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads one effect per `effect/<effect id>.toml` resource. */
class ResourcesEffectLoader(
    private val effectRoot: VfsFile = resourcesVfs["effect"],
) : EffectLoader {
    private var effects = emptyList<Effect.Dto>()

    override suspend fun initResources() {
        effects =
            effectRoot.readTomlFiles<EffectDefinition>().map { (effectId, definition) ->
                fromResource("effect/$effectId.toml") { definition.toDto(effectId) }
            }
    }

    override fun loadEffects(): List<Effect.Dto> = effects

    @Serializable
    data class EffectDefinition(
        val outcome: OutcomeDefinition,
        val application: ApplicationDefinition,
    ) {
        fun toDto(effectId: String): Effect.Dto =
            Effect.Dto(
                id = effectId,
                outcome = outcome.toDto(),
                application = application.toDto(),
            )
    }

    // Only the fields required by the outcome type are set, e.g.: `damage` for `DECREASE_HEALTH`.
    @Serializable
    data class OutcomeDefinition(
        val type: String,
        val damage: Int? = null,
        val healing: Int? = null,
        val effectId: String? = null,
        val unitId: String? = null,
        val mana: Int? = null,
        val healthPoints: Int? = null,
        val manaPoints: Int? = null,
        val abilityId: String? = null,
    ) {
        fun toDto(): EffectOutcomeDto {
            val outcomeType = enumOf<TypeDto>("outcome.type", type)
            return EffectOutcomeDto(
                type = outcomeType,
                decreaseHealth = if (outcomeType == TypeDto.DECREASE_HEALTH) DecreaseHealthDto(required("outcome.damage", damage)) else null,
                increaseHealth = if (outcomeType == TypeDto.INCREASE_HEALTH) IncreaseHealthDto(required("outcome.healing", healing)) else null,
                applyEffectOnNearbyAllies =
                    if (outcomeType == TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES) {
                        ApplyEffectOnNearbyAlliesDto(required("outcome.effectId", effectId))
                    } else {
                        null
                    },
                deployBattleUnit = if (outcomeType == TypeDto.DEPLOY_BATTLE_UNIT) DeployBattleUnitDto(required("outcome.unitId", unitId)) else null,
                increaseMana = if (outcomeType == TypeDto.INCREASE_MANA) IncreaseManaDto(required("outcome.mana", mana)) else null,
                increaseMaximumHealth =
                    if (outcomeType == TypeDto.INCREASE_MAXIMUM_HEALTH) {
                        IncreaseMaximumHealthDto(required("outcome.healthPoints", healthPoints))
                    } else {
                        null
                    },
                increaseMaximumMana =
                    if (outcomeType == TypeDto.INCREASE_MAXIMUM_MANA) {
                        IncreaseMaximumManaDto(required("outcome.manaPoints", manaPoints))
                    } else {
                        null
                    },
                learnAbility = if (outcomeType == TypeDto.LEARN_ABILITY) LearnAbilityDto(required("outcome.abilityId", abilityId)) else null,
            )
        }
    }

    // Only the fields required by the application type are set, e.g.: `duration` for `ON_TURN_STARTED`.
    @Serializable
    data class ApplicationDefinition(
        val type: String,
        val duration: Int? = null,
    ) {
        fun toDto(): ApplicationDto {
            val applicationType = enumOf<ApplicationTypeDto>("application.type", type)
            return ApplicationDto(
                type = applicationType,
                onTurnStarted =
                    if (applicationType == ApplicationTypeDto.ON_TURN_STARTED) {
                        OnTurnStartedDto(required("application.duration", duration))
                    } else {
                        null
                    },
                beforeApplyingEffect =
                    if (applicationType == ApplicationTypeDto.BEFORE_APPLYING_EFFECT) {
                        BeforeApplyingEffectDto(required("application.duration", duration))
                    } else {
                        null
                    },
            )
        }
    }
}
