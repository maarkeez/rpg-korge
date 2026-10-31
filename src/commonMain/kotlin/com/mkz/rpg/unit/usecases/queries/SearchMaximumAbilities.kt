package com.mkz.rpg.unit.usecases.queries

import com.mkz.rpg.unit.domain.Unit

/** The most abilities a battle unit knows, the same for every unit, so the other subdomains do not read the rule of [Unit]. */
class SearchMaximumAbilities {
    operator fun invoke(): Int = Unit.maximumAbilities()
}
