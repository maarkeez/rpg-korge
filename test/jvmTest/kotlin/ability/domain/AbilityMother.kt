package ability.domain

import effect.domain.EffectMother.effectId
import korlibs.crypto.SecureRandom.nextInt
import korlibs.io.util.UUID

object AbilityMother {

    fun ability(
        id: String = id(),
        name: String = name(),
        cost: Int = cooldown(),
        cooldown: Int = cost(),
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

    fun id() = "ability-${nextInt(from = 1, until = 100)}"
    fun name() = "Ability ${UUID.randomUUID().toString().takeLast(5)}"
    fun cost() = nextInt(from = 0, until = 999)
    fun cooldown() = nextInt(from = 0, until = 99)
    fun effects() = List(nextInt(from = 1, until = 4)){ effectId() }
    fun targetPattern() = listOf("SELF","ADJACENT_ENEMY","VACANT_TILE_ADJACENT_TO_BATTLE_UNIT").random()
}
