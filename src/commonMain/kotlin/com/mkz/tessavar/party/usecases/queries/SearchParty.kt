package com.mkz.tessavar.party.usecases.queries

import com.mkz.tessavar.party.domain.Party
import com.mkz.tessavar.party.domain.PartyRepository

class SearchParty(
    private val partyRepository: PartyRepository,
) {
    operator fun invoke(): Party.Dto? = partyRepository.search()?.toDto()
}
