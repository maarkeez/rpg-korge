package com.mkz.rpg.quest.adapters.resources

import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.PositionDto
import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.RewardDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
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
    fun `should load a main quest when its file holds one task and a reward of gold`() =
        runTest {
            // Given
            val toml =
                """
                type = "MAIN"

                [[tasks]]
                type = "DEFEAT_UNITS"
                unitId = "goat-wizard"
                count = 1

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
                    giverNpcId = null,
                    repeatable = false,
                    tasks = listOf(TaskDto.defeatUnits(unitId = "goat-wizard", count = 1)),
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
                giverNpcId = "farmer"
                repeatable = true

                [[tasks]]
                type = "REACH_TILES"
                tiles = [[17, 11], [18, 11]]

                [[tasks]]
                type = "TALK_TO_NPC"
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
                    giverNpcId = "farmer",
                    repeatable = true,
                    tasks =
                        listOf(
                            TaskDto.reachTiles(tiles = listOf(PositionDto(row = 17, column = 11), PositionDto(row = 18, column = 11))),
                            TaskDto.talkToNpc(npcId = "farmer"),
                        ),
                    reward = RewardDto(gold = 20, lootableItemIds = listOf("health-potion")),
                    progress = ProgressDto.notStarted("help-the-farmer"),
                ),
            )
        }

    @Test
    fun `should fail with the file when a defeat task has no count`() =
        runTest {
            // Given
            val toml = "type = \"MAIN\"\n\n[[tasks]]\ntype = \"DEFEAT_UNITS\"\nunitId = \"rat\"\n\n[reward]\ngold = 1\n"
            // When
            val loading = suspend { load("defeat-a-rat", toml) }
            // Then
            shouldThrow<IllegalStateException> { loading() }.message.shouldContain("quest/defeat-a-rat.toml")
        }

    @Test
    fun `should fail with the file when a tile is not a row and a column`() =
        runTest {
            // Given
            val toml = "type = \"MAIN\"\n\n[[tasks]]\ntype = \"REACH_TILES\"\ntiles = [[1, 2, 3]]\n\n[reward]\ngold = 1\n"
            // When
            val loading = suspend { load("reach-a-tile", toml) }
            // Then
            shouldThrow<IllegalStateException> { loading() }.message.shouldContain("quest/reach-a-tile.toml")
        }

    @Test
    fun `should fail with the file when its type is unknown`() =
        runTest {
            // Given
            val toml = "type = \"SECRET\"\n\n[[tasks]]\ntype = \"TALK_TO_NPC\"\nnpcId = \"farmer\"\n\n[reward]\ngold = 1\n"
            // When
            val loading = suspend { load("a-secret", toml) }
            // Then
            shouldThrow<IllegalStateException> { loading() }.message.shouldContain("quest/a-secret.toml")
        }
}
