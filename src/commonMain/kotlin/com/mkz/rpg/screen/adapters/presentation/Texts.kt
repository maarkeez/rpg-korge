package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto

/**
 * The texts the screen displays, translated into the selected language, e.g.: `texts[UiText.SHOP_NOT_ENOUGH_GOLD]`.
 * The placeholders of a text are replaced by the arguments of the same name, e.g.: `texts[UiText.BATTLE_INFO_ROUND, "round" to 3]`.
 */
class Texts(
    private val localizationApi: LocalizationApi,
) {
    operator fun get(
        text: UiText,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translate(text.key, arguments.toArguments())

    /** The plural form of the [text] for [count], e.g.: `Vanishes in 3 turns`. */
    fun plural(
        text: UiText,
        count: Int,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translatePlural(text.key, count, arguments.toArguments())

    /** An amount of gold, e.g.: `50 G`. */
    fun gold(amount: Int): String = get(UiText.GOLD, "gold" to amount)

    // The game data, translated from its id, e.g.: `unit.bat.name`. See docs/architecture/localization.md.
    fun unitName(unitId: String): String = localizationApi.translate("unit.$unitId.name")

    fun lootableItemName(lootableItemId: String): String = localizationApi.translate("lootableItem.$lootableItemId.name")

    fun npcName(npcId: String): String = localizationApi.translate("npc.$npcId.name")

    fun npcLine(npcId: String): String = localizationApi.translate("npc.$npcId.line")

    fun playerName(playerId: String): String = localizationApi.translate("player.$playerId.name")

    fun terrainName(terrainId: String): String = localizationApi.translate("terrain.$terrainId.name")

    /** The languages the game can be played in, English first, each named in its own language, e.g.: `Español`. */
    fun languages(): List<LanguageDto> = localizationApi.searchLanguages()

    /** Translates every text into the language from now on, e.g.: `es`, for the rest of the session. */
    fun selectLanguage(languageCode: String) = localizationApi.selectLanguage(languageCode)

    private fun Array<out Pair<String, Any>>.toArguments(): Map<String, String> = associate { (name, value) -> name to value.toString() }
}
