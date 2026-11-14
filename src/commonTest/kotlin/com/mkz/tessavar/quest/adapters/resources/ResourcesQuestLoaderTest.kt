package com.mkz.tessavar.quest.adapters.resources

import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.Quest.Dto.PositionDto
import com.mkz.tessavar.quest.domain.Quest.Dto.ProgressDto
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.tessavar.quest.domain.Quest.Dto.RewardDto
import com.mkz.tessavar.quest.domain.Quest.Dto.TaskDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import korlibs.io.file.std.MemoryVfsMix
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResourcesQuestLoaderTest {
    private suspend fun load(
        questId: String,
        toml: String,
    ): List<Quest.Dto> {
        val loader = ResourcesQuestLoader(questRoot = MemoryVfsMix("quest/$questId.toml" to toml)["quest"])
        loader.initResources()
        return loader.loadQuests()
    }

    @Test
    fun `should load a main quest with its start tiles when its file holds them, one task and a reward of gold`() =
        runTest {
            // Given
            val toml =
                """
                type = "MAIN"
                startTiles = [[14, 9], [14, 10]]

                [[tasks]]
                type = "DEFEAT"
                unitId = "goat-wizard"
                requiredDefeats = 1

                [reward]
                gold = 150
                """.trimIndent()
            // When
            val quests = load("defeat-the-goat-wizard", toml)
            // Then
            quests.shouldContainExactly(
                Quest.Dto(
                    id = "defeat-the-goat-wizard",
                    type = QuestTypeDto.MAIN,
                    questGiverNpcId = null,
                    startTiles = listOf(PositionDto(row = 14, column = 9), PositionDto(row = 14, column = 10)),
                    repeatable = false,
                    tasks = listOf(TaskDto.defeat(unitId = "goat-wizard", requiredDefeats = 1)),
                    reward = RewardDto(gold = 150, lootableItemIds = emptyList()),
                    progress = ProgressDto.notStarted("defeat-the-goat-wizard"),
                ),
            )
        }

    @Test
    fun `should load a side quest with its tiles nested inside its tasks when its file lists them`() =
        runTest {
            // Given
            val toml =
                """
                type = "SIDE"
                questGiverNpcId = "farmer"
                repeatable = true

                [[tasks]]
                type = "VISIT"
                tiles = [[17, 11], [18, 11]]

                [[tasks]]
                type = "TALK"
                npcId = "farmer"

                [reward]
                gold = 20
                lootableItemIds = ["health-potion"]
                """.trimIndent()
            // When
            val quests = load("help-the-farmer", toml)
            // Then
            quests.shouldContainExactly(
                Quest.Dto(
                    id = "help-the-farmer",
                    type = QuestTypeDto.SIDE,
                    questGiverNpcId = "farmer",
                    startTiles = emptyList(),
                    repeatable = true,
                    tasks =
                        listOf(
                            TaskDto.visit(tiles = listOf(PositionDto(row = 17, column = 11), PositionDto(row = 18, column = 11))),
                            TaskDto.talk(npcId = "farmer"),
                        ),
                    reward = RewardDto(gold = 20, lootableItemIds = listOf("health-potion")),
                    progress = ProgressDto.notStarted("help-the-farmer"),
                ),
            )
        }

    @Test
    fun `should fail with the file when a defeat task has no required defeats`() =
        runTest {
            // Given
            val toml = "type = \"MAIN\"\n\n[[tasks]]\ntype = \"DEFEAT\"\nunitId = \"rat\"\n\n[reward]\ngold = 1\n"
            // When
            val loading = suspend { load("defeat-a-rat", toml) }
            // Then
            shouldThrow<IllegalStateException> { loading() }.message.shouldContain("quest/defeat-a-rat.toml")
        }

    @Test
    fun `should fail with the file when a tile is not a row and a column`() =
        runTest {
            // Given
            val toml = "type = \"MAIN\"\n\n[[tasks]]\ntype = \"VISIT\"\ntiles = [[1, 2, 3]]\n\n[reward]\ngold = 1\n"
            // When
            val loading = suspend { load("visit-a-tile", toml) }
            // Then
            shouldThrow<IllegalStateException> { loading() }.message.shouldContain("quest/visit-a-tile.toml")
        }

    @Test
    fun `should fail with the file when its type is unknown`() =
        runTest {
            // Given
            val toml = "type = \"SECRET\"\n\n[[tasks]]\ntype = \"TALK\"\nnpcId = \"farmer\"\n\n[reward]\ngold = 1\n"
            // When
            val loading = suspend { load("a-secret", toml) }
            // Then
            shouldThrow<IllegalStateException> { loading() }.message.shouldContain("quest/a-secret.toml")
        }
}
