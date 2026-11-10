package com.mkz.rpg.party.adapters.storage

import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository

class InMemoryPartyRepository : PartyRepository {
    private var party: Party? = null

    override fun save(party: Party) {
        this.party = party
    }

    override fun search() = party
}
