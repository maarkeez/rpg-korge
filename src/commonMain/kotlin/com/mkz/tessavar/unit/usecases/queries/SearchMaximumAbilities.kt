package com.mkz.tessavar.unit.usecases.queries

import com.mkz.tessavar.unit.domain.Unit

class SearchMaximumAbilities {
    operator fun invoke(): Int = Unit.maximumAbilities()
}
