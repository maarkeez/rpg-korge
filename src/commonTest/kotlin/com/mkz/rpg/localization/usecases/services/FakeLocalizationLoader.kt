package com.mkz.rpg.localization.usecases.services

import com.mkz.rpg.localization.domain.Localization

// Loads the given localization, instead of reading its catalogs from the resources.
class FakeLocalizationLoader(
    private val localization: Localization.Dto,
) : LocalizationLoader {
    override fun loadLocalization(): Localization.Dto = localization
}
