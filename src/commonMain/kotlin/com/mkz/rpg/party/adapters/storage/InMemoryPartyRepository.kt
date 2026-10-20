package com.mkz.rpg.party.adapters.storage

import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository

// Holds the party of the game in progress.
class InMemoryPartyRepository : PartyRepository {
    private var party: Party? = null

    override fun save(party: Party) {
        this.party = party
    }

    override fun search() = party
}
