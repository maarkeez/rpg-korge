package com.mkz.rpg.party.domain

sealed class PartyError(
    message: String,
) : Throwable(message = message) {
    class PartyRequired : PartyError("At least one battle unit is required to explore")

    class NotExploring : PartyError("The party can only step while exploring")

    class AlreadyStepping : PartyError("The party can not step again until its party step is done")

    class NotAFollower : PartyError("The leader can only swap tiles with a follower of the party")

    class StepsRequired : PartyError("At least one step is required for a party step")

    class NotTheParty : PartyError("The formation must order every battle unit of the party once")
}
