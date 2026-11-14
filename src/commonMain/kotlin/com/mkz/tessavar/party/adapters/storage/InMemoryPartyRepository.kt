package com.mkz.tessavar.party.adapters.storage

import com.mkz.tessavar.party.domain.Party
import com.mkz.tessavar.party.domain.PartyRepository

class InMemoryPartyRepository : PartyRepository {
    private var party: Party? = null

    override fun save(party: Party) {
        this.party = party
    }

    override fun search() = party
}
