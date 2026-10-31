package com.mkz.rpg.party.usecases.queries

import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository

/** What is saved of the party exploring, if any: its members and its line. */
class SearchPartySnapshot(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(): Party.Dto.SnapshotDto? = partyRepository.search()?.toSnapshot()
}
