package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.localization.adapters.presentation.LocalizationApi
import com.mkz.rpg.localization.domain.Localization.Dto.LanguageDto

class Messages(
    private val localizationApi: LocalizationApi,
) {
    operator fun get(
        text: UiMessage,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translate(text.key, arguments.toArguments())

    fun plural(
        text: UiMessage,
        count: Int,
        vararg arguments: Pair<String, Any>,
    ): String = localizationApi.translatePlural(text.key, count, arguments.toArguments())

    fun gold(amount: Int): String = get(UiMessage.GOLD, "gold" to amount)

    fun unitName(unitId: String): String = localizationApi.translate("unit.$unitId.name")

    fun abilityName(abilityId: String): String = localizationApi.translate("ability.$abilityId.name")

    fun lootableItemName(lootableItemId: String): String = localizationApi.translate("lootableItem.$lootableItemId.name")

    fun npcName(npcId: String): String = localizationApi.translate("npc.$npcId.name")

    fun npcOwnLine(npcId: String): String = localizationApi.translate("npc.$npcId.line")

    fun playerName(playerId: String): String = localizationApi.translate("player.$playerId.name")

    fun terrainName(terrainId: String): String = localizationApi.translate("terrain.$terrainId.name")

    fun questName(questId: String): String = localizationApi.translate("quest.$questId.name")

    fun questDescription(questId: String): String = localizationApi.translate("quest.$questId.description")

    fun questTask(
        questId: String,
        taskIndex: Int,
    ): String = localizationApi.translate("quest.$questId.task.$taskIndex")

    fun questLine(
        questId: String,
        line: String,
    ): String = localizationApi.translate("quest.$questId.line.$line")

    fun languages(): List<LanguageDto> = localizationApi.searchLanguages()

    fun selectLanguage(languageCode: String) = localizationApi.selectLanguage(languageCode)

    private fun Array<out Pair<String, Any>>.toArguments(): Map<String, String> = associate { (name, value) -> name to value.toString() }
}
