package com.mkz.rpg.localization.domain

/** Holds the single localization of the game. */
interface LocalizationRepository {
    fun save(localization: Localization)

    fun search(): Localization?
}
