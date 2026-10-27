package com.mkz.rpg.effect.adapters.resources

import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.BeforeApplyingEffectDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.usecases.services.EffectLoader
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import com.mkz.rpg.shared.adapters.toml.required
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

/** Loads one effect per `effect/<effect id>.toml` resource. */
class ResourcesEffectLoader(
    private val effectRoot: VfsFile = ResourcesBundle.root["effect"],
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
        fun toDto(): EffectOutcomeDto =
            when (enumOf<TypeDto>("outcome.type", type)) {
                TypeDto.DECREASE_HEALTH -> EffectOutcomeDto.decreaseHealth(damage = required("outcome.damage", damage))
                TypeDto.INCREASE_HEALTH -> EffectOutcomeDto.increaseHealth(healing = required("outcome.healing", healing))
                TypeDto.NEGATE_INCREASE_HEALTH -> EffectOutcomeDto.negateIncreaseHealth()
                TypeDto.TELEPORT -> EffectOutcomeDto.teleport()
                TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES -> EffectOutcomeDto.applyEffectOnNearbyAllies(effectId = required("outcome.effectId", effectId))
                TypeDto.DEPLOY_BATTLE_UNIT -> EffectOutcomeDto.deployBattleUnit(unitId = required("outcome.unitId", unitId))
                TypeDto.INCREASE_MANA -> EffectOutcomeDto.increaseMana(mana = required("outcome.mana", mana))
                TypeDto.INCREASE_MAXIMUM_HEALTH -> EffectOutcomeDto.increaseMaximumHealth(healthPoints = required("outcome.healthPoints", healthPoints))
                TypeDto.INCREASE_MAXIMUM_MANA -> EffectOutcomeDto.increaseMaximumMana(manaPoints = required("outcome.manaPoints", manaPoints))
                TypeDto.LEARN_ABILITY -> EffectOutcomeDto.learnAbility(abilityId = required("outcome.abilityId", abilityId))
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
