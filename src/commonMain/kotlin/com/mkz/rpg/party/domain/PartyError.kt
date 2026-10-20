package com.mkz.rpg.party.domain

sealed class PartyError(
    message: String,
) : Throwable(message = message) {
    class PartyRequired : PartyError("At least one battle unit is required to explore")

    class NotExploring : PartyError("The party can only walk while exploring")

    class AlreadyWalking : PartyError("The party can not walk again until it has finished walking")

    class StepsRequired : PartyError("At least one step is required to walk")
}
