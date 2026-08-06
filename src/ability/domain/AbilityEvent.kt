package ability.domain

sealed interface AbilityEvent {
    data class AbilityCreated(
        val abilityId: String,
    ): AbilityEvent
}
