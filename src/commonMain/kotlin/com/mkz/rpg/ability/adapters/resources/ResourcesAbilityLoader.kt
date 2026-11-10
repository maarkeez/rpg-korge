package com.mkz.rpg.ability.adapters.resources

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.AbilityEffectDto
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.usecases.services.AbilityLoader
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

class ResourcesAbilityLoader(
    private val abilityRoot: VfsFile = ResourcesBundle.root["ability"],
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
        val manaCost: Int,
        val cooldown: Int,
        val targeting: String,
        val effects: List<AbilityEffectDefinition>,
        val summonDuration: Int? = null,
    ) {
        fun toDto(abilityId: String): Ability.Dto =
            Ability.Dto(
                id = abilityId,
                manaCost = manaCost,
                cooldown = cooldown,
                targeting = enumOf<TargetingDto>("targeting", targeting),
                abilityEffects = effects.map { it.toDto() },
                summonDuration = summonDuration,
            )
    }

    @Serializable
    data class AbilityEffectDefinition(
        val effectId: String,
        val target: String,
    ) {
        fun toDto(): AbilityEffectDto =
            AbilityEffectDto(
                effectId = effectId,
                target = EffectTargetDto(type = enumOf<EffectTargetDto.Type>("effects.target", target)),
            )
    }
}
