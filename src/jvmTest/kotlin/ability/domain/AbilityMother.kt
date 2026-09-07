package ability.domain

import effect.domain.EffectMother.effectId
import korlibs.io.util.*
import kotlin.random.*

object AbilityMother {

    fun ability(
        id: String = id(),
        name: String = name(),
        cost: Int = cost(),
        cooldown: Int = cooldown(),
        effects: List<String> = effects(),
        targetPattern: String = targetPattern(),
    )= Ability.create(dto=Ability.Dto(
        id = id,
        name = name,
        cost = cost,
        cooldown = cooldown,
        effects = effects,
        targetPattern = targetPattern,
    ))

    fun id() = "ability-${Random.nextInt(1, 100)}"
    fun name() = "Ability ${UUID.randomUUID().toString().takeLast(5)}"
    fun cost() = Random.nextInt(0, 999)
    fun cooldown() = Random.nextInt(0, 99)
    fun effects() = List(Random.nextInt(1, 4)){ effectId() }
    fun targetPattern() = listOf("SELF","ADJACENT_ENEMY","VACANT_TILE_ADJACENT_TO_BATTLE_UNIT").random()
}
