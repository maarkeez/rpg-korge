package com.mkz.rpg.party.usecases.queries

import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository

class SearchPartySnapshot(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(): Party.Dto.SnapshotDto? = partyRepository.search()?.toSnapshot()
}
