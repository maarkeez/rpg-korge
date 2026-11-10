package com.mkz.rpg.unit.usecases.queries

import com.mkz.rpg.unit.domain.Unit

class SearchMaximumAbilities {
    operator fun invoke(): Int = Unit.maximumAbilities()
}
