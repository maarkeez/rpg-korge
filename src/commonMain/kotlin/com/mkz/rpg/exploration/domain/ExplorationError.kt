package com.mkz.rpg.exploration.domain

sealed class ExplorationError(
    message: String,
) : Throwable(message = message) {
    class PartyRequired : ExplorationError("At least one battle unit is required to explore")

    class NotExploring : ExplorationError("The party can only walk while exploring")

    class AlreadyWalking : ExplorationError("The party can not walk again until it has finished walking")

    class StepsRequired : ExplorationError("At least one step is required to walk")
}
