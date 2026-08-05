package ability.domain

import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Ability private constructor(
    private val id: Id,
    private val name: Name,
    private val cost: Cost,
    private val cooldown: Cooldown,
    private val effects: Effects,
) {
    
    @JvmInline value class Id(val value: String)
    @JvmInline value class Name(val value: String)
    @JvmInline value class Cost(val value: Int)
    @JvmInline value class Cooldown(val value: Int)
    @JvmInline value class Effects(val value: List<String>)

    enum class TargetPattern {
        SELF,
        ADJACENT_ENEMY,
        BATTLE_UNIT_WITH_ADJACENT_TILE_VACANT
    }
}
