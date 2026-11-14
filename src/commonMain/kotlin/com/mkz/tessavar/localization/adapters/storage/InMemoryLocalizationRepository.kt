package com.mkz.tessavar.localization.adapters.storage

import com.mkz.tessavar.localization.domain.Localization
import com.mkz.tessavar.localization.domain.LocalizationRepository

class InMemoryLocalizationRepository : LocalizationRepository {
    private var localization: Localization? = null

    override fun save(localization: Localization) {
        this.localization = localization
    }

    override fun search(): Localization? = localization
}
