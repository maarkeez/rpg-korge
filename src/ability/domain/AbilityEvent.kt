package ability.domain

interface AbilityEvent {
    data class AbilityCreated(
        val abilityId: String,
    ): AbilityEvent
}
