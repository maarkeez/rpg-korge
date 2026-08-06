package ability.domain

import ability.domain.AbilityError.AbilityCooldownAboveLimit
import ability.domain.AbilityError.AbilityCostAboveLimit
import ability.domain.AbilityError.AbilityEffectsAboveLimit
import ability.domain.AbilityError.AbilityNameTooLong
import ability.domain.AbilityError.EmptyAbilityId
import ability.domain.AbilityError.EmptyAbilityName
import ability.domain.AbilityError.AbilityEmptyEffects
import ability.domain.AbilityError.InvalidTargetPattern
import ability.domain.AbilityError.NegativeAbilityCooldown
import ability.domain.AbilityError.NegativeAbilityCost
import ability.domain.AbilityEvent.AbilityCreated
import kotlin.jvm.JvmInline

@ConsistentCopyVisibility
data class Ability private constructor(
    private val id: Id,
    private val name: Name,
    private val cost: Cost,
    private val cooldown: Cooldown,
    private val effects: Effects,
    private val targetPattern: TargetPattern,
    private val events: Set<AbilityEvent>,
) {

    companion object {
        fun create(dto: Dto): Ability {
            return Ability(
                id = Id(dto.id),
                name = Name(dto.name),
                cost = Cost(dto.cost),
                cooldown = Cooldown(dto.cooldown),
                effects = Effects(dto.effects),
                targetPattern = TargetPattern(dto.targetPattern),
                events = setOf(AbilityCreated(abilityId = dto.id))
            )
        }
    }

    fun toDto() = Dto(
        id = id.value,
        name = name.value,
        cost = cost.value,
        cooldown = cooldown.value,
        effects = effects.value,
        targetPattern = targetPattern.toDto()
    )

    fun pullEvents() = events to copy(events = emptySet())
    
    @JvmInline private value class Id(val value: String){
        init {
            if(value.isBlank()) throw EmptyAbilityId()
        }
    }
    @JvmInline private value class Name(val value: String)
    {
        init {
            if(value.isBlank()) throw EmptyAbilityName()
            if(value.count() > 50) throw AbilityNameTooLong()
        }
    }
    @JvmInline private value class Cost(val value: Int){
        init {
            if(value < 0) throw NegativeAbilityCost()
            if(value > 999) throw AbilityCostAboveLimit()
        }
    }
    @JvmInline private value class Cooldown(val value: Int){
        init {
            if(value < 0) throw NegativeAbilityCooldown()
            if(value > 99) throw AbilityCooldownAboveLimit()
        }
    }
    @JvmInline private value class Effects(val value: List<String>){
        init {
            if(value.isEmpty()) throw AbilityEmptyEffects()
            if(value.size > 3) throw AbilityEffectsAboveLimit()
        }
    }

    private enum class TargetPattern {
        SELF,
        ADJACENT_ENEMY,
        BATTLE_UNIT_WITH_ADJACENT_TILE_VACANT;

        companion object {
            operator fun invoke(targetPattern:String): TargetPattern =  runCatching { TargetPattern.valueOf(targetPattern) }.getOrElse { throw InvalidTargetPattern() }
        }

        fun toDto() = name
    }

    data class Dto(
        val id: String,
        val name: String,
        val cost: Int,
        val cooldown: Int,
        val effects: List<String>,
        val targetPattern: String,
    )
}
