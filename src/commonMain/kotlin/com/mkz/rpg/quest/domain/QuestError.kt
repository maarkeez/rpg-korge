package com.mkz.rpg.quest.domain

sealed class QuestError(
    message: String,
) : Throwable(message = message) {
    class QuestWithoutTasks : QuestError("A quest needs at least one task")

    class EmptyQuestReward : QuestError("A quest needs a reward of some gold or a lootable item")

    class NegativeQuestRewardGold : QuestError("The gold of a quest reward is negative")

    class InvalidTask : QuestError("A task needs the details of its type")

    class EmptyTaskTiles : QuestError("A task to reach tiles needs at least one tile")

    class TaskCountBelowOne : QuestError("A task to defeat units needs a count of at least 1")

    class SideQuestWithoutGiver : QuestError("A side quest needs a quest giver")

    class MainQuestWithGiver : QuestError("The main quest starts with a new game, so it has no quest giver")

    class MainQuestRepeatable : QuestError("The main quest runs once, so it is not repeatable")

    class MainQuestNotUnique : QuestError("The game needs exactly one main quest")

    class MainQuestCanNotBeAbandoned : QuestError("The main quest can not be abandoned")

    class QuestNotActive : QuestError("Only an active quest can be abandoned")

    class QuestNotFound : QuestError("The quest was not found")

    class QuestTileCanNotBeOccupied : QuestError("A tile of a task to reach tiles can not be occupied")
}
