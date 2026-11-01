package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.Kind
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.RewardItem
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.Task
import com.mkz.rpg.screen.adapters.presentation.Texts

private const val GOAT_WIZARD_QUEST = "defeat-the-goat-wizard"
private const val GOAT_WIZARD_PORTRAIT = "unit/goat-wizard_portrait.png"
private const val RAT_PORTRAIT = "unit/rat_portrait.png"
private const val ELDER_PORTRAIT = "npc/elder_portrait.png"

// The quest the game ships, with the texts of the language global, which the other samples are built on.
private fun goatWizard(texts: Texts) =
    QuestInfo(
        name = texts.questName(GOAT_WIZARD_QUEST),
        description = texts.questDescription(GOAT_WIZARD_QUEST),
        tasks = listOf(goatWizardTask(texts)),
        gold = 150,
    )

private fun goatWizardTask(texts: Texts) = Task(Kind.DEFEAT, texts.questTask(GOAT_WIZARD_QUEST, 0), portraitPath = GOAT_WIZARD_PORTRAIT)

/** The quests the stories display: the one the game ships, and the others the backend allows. */
enum class QuestSample(
    val title: String,
    val state: String,
    val quest: (Texts) -> QuestInfo,
) {
    GOAT_WIZARD("Goat wizard", "The quest the game ships: one task, gold only.", ::goatWizard),
    COUNTED(
        "Counted",
        "A task with a count, under way: 3 of 5.",
        { texts -> goatWizard(texts).copy(tasks = listOf(Task(Kind.DEFEAT, "Defeat 5 Rats", progress = 3, count = 5, portraitPath = RAT_PORTRAIT))) },
    ),
    TALK(
        "Talk",
        "A task to talk to an npc.",
        { texts -> goatWizard(texts).copy(tasks = listOf(Task(Kind.TALK, "Talk to the Elder", portraitPath = ELDER_PORTRAIT))) },
    ),
    REACH(
        "Reach",
        "A task to reach some tiles: it has no portrait.",
        { texts -> goatWizard(texts).copy(tasks = listOf(Task(Kind.REACH, "Reach the lair down south"))) },
    ),
    SEVERAL_TASKS(
        "Several tasks",
        "Two tasks done and the current one: those still to come are not revealed.",
        { texts ->
            goatWizard(texts).copy(
                tasks =
                    listOf(
                        Task(Kind.TALK, "Talk to the Elder", progress = 1, portraitPath = ELDER_PORTRAIT, isDone = true),
                        Task(Kind.REACH, "Reach the lair down south", progress = 1, isDone = true),
                        goatWizardTask(texts),
                    ),
            )
        },
    ),
    ITEM_REWARDS(
        "Item rewards",
        "A reward of gold and items.",
        { texts ->
            goatWizard(texts).copy(
                items = listOf("rune-hammer", "health-elixir", "bee-essence").map { lootableItemId -> RewardItem(lootableItemId, texts.lootableItemName(lootableItemId)) },
            )
        },
    ),
    LONG_TEXT(
        "Long text",
        "The longest texts, as a translation may be: a long name, description and task, and the largest gold.",
        {
            QuestInfo(
                name = "La guarida del mago cabra en las montañas del sur",
                description = "Hace mucho tiempo una cabra se comió el libro de hechizos de un mago y se tragó toda su magia. Ahora gobierna la guarida del sur, y nadie se atreve a acercarse.",
                tasks = listOf(Task(Kind.DEFEAT, "Derrota al mago cabra de la guarida del sur", portraitPath = GOAT_WIZARD_PORTRAIT)),
                gold = 99_999,
            )
        },
    ),
    COMPLETED(
        "Completed",
        "The goat wizard defeated and its reward paid.",
        { texts ->
            goatWizard(texts).let { quest -> quest.copy(isCompleted = true, tasks = quest.tasks.map { task -> task.copy(isDone = true, progress = task.count) }) }
        },
    ),
}
