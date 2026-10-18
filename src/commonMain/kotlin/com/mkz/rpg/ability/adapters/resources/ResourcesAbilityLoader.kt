package com.mkz.rpg.ability.adapters.resources

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectSpecDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.usecases.services.AbilityLoader
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads one ability per `ability/<ability id>.toml` resource. */
class ResourcesAbilityLoader(
    private val abilityRoot: VfsFile = resourcesVfs["ability"],
) : AbilityLoader {
    private var abilities = emptyList<Ability.Dto>()

    override suspend fun initResources() {
        abilities =
            abilityRoot.readTomlFiles<AbilityDefinition>().map { (abilityId, definition) ->
                fromResource("ability/$abilityId.toml") { definition.toDto(abilityId) }
            }
    }

    override fun loadAbilities(): List<Ability.Dto> = abilities

    @Serializable
    data class AbilityDefinition(
        val name: String,
        val cost: Int,
        val cooldown: Int,
        val targeting: String,
        val effects: List<EffectSpecDefinition>,
        val turnsDuration: Int? = null,
    ) {
        fun toDto(abilityId: String): Ability.Dto =
            Ability.Dto(
                id = abilityId,
                name = name,
                cost = cost,
                cooldown = cooldown,
                targeting = enumOf<TargetingDto>("targeting", targeting),
                effectSpecs = effects.map { it.toDto() },
                turnsDuration = turnsDuration,
            )
    }

    @Serializable
    data class EffectSpecDefinition(
        val effectId: String,
        val target: String,
    ) {
        fun toDto(): EffectSpecDto =
            EffectSpecDto(
                effectId = effectId,
                target = TargetExpressionDto(type = enumOf<TargetExpressionDto.Type>("effects.target", target)),
            )
    }
}
