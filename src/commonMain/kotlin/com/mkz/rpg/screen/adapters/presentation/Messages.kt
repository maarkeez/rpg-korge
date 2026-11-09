package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto

/**
 * The messages the screen displays, translated into the selected language, e.g.: `messages[UiMessage.SHOP_NOT_ENOUGH_GOLD]`.
 * The placeholders of a text are replaced by the arguments of the same name, e.g.: `messages[UiMessage.BANNER_ROUND, "round" to 3]`.
 */
class Messages(
    private val localizationApi: LocalizationApi,
) {
    operator fun get(
        text: UiMessage,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translate(text.key, arguments.toArguments())

    /** The plural form of the [text] for [count], e.g.: `Vanishes in 3 turns`. */
    fun plural(
        text: UiMessage,
        count: Int,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translatePlural(text.key, count, arguments.toArguments())

    /** An amount of gold, e.g.: `50 G`. */
    fun gold(amount: Int): String = get(UiMessage.GOLD, "gold" to amount)

    // The game data, translated from its id, e.g.: `unit.bat.name`.
    fun unitName(unitId: String): String = localizationApi.translate("unit.$unitId.name")

    fun abilityName(abilityId: String): String = localizationApi.translate("ability.$abilityId.name")

    fun lootableItemName(lootableItemId: String): String = localizationApi.translate("lootableItem.$lootableItemId.name")

    fun npcName(npcId: String): String = localizationApi.translate("npc.$npcId.name")

    fun npcLine(npcId: String): String = localizationApi.translate("npc.$npcId.line")

    fun playerName(playerId: String): String = localizationApi.translate("player.$playerId.name")

    fun terrainName(terrainId: String): String = localizationApi.translate("terrain.$terrainId.name")

    fun questName(questId: String): String = localizationApi.translate("quest.$questId.name")

    fun questDescription(questId: String): String = localizationApi.translate("quest.$questId.description")

    /** A task of the quest, by its place in the line of tasks, as a whole sentence, e.g.: `Defeat the Goat Wizard`. */
    fun questTask(
        questId: String,
        taskIndex: Int,
    ): String = localizationApi.translate("quest.$questId.task.$taskIndex")

    /** The languages the game can be played in, English first, each named in its own language, e.g.: `Español`. */
    fun languages(): List<LanguageDto> = localizationApi.searchLanguages()

    /** Translates every text into the language from now on, e.g.: `es`, for the rest of the session. */
    fun selectLanguage(languageCode: String) = localizationApi.selectLanguage(languageCode)

    private fun Array<out Pair<String, Any>>.toArguments(): Map<String, String> = associate { (name, value) -> name to value.toString() }
}
