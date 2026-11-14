package com.mkz.tessavar.party.domain

interface PartyRepository {
    fun save(party: Party)

    fun search(): Party?
}
