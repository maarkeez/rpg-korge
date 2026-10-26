package com.mkz.rpg.localization.adapters.storage

import com.mkz.rpg.localization.domain.Localization
import com.mkz.rpg.localization.domain.LocalizationRepository

class InMemoryLocalizationRepository : LocalizationRepository {
    private var localization: Localization? = null

    override fun save(localization: Localization) {
        this.localization = localization
    }

    override fun search(): Localization? = localization
}
