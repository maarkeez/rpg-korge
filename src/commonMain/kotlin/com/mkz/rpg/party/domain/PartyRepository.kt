package com.mkz.rpg.party.domain

interface PartyRepository {
    fun save(party: Party)

    fun search(): Party?
}
