package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.xy

// Page object of [QuestAnnouncementView].

fun QuestAnnouncementView.isAnnouncementShown(): Boolean = isShown()

fun QuestAnnouncementView.displayedTitle(): String? = titleText?.text

fun QuestAnnouncementView.displayedName(): String? = nameText?.text

/** The current task, or `null` when it announces the completion. */
fun QuestAnnouncementView.displayedTask(): String? = taskText?.text

/** The gold of the reward, e.g.: `150 G`, or none when it pays none or it announces the start. */
fun QuestAnnouncementView.displayedGold(): List<String> =
    rewardView
        ?.descendants()
        .orEmpty()
        .filterIsInstance<Text>()
        .map { line -> line.text }

/** How many items of the reward it shows, each in its slot. */
fun QuestAnnouncementView.displayedItemSlots(): Int = rewardView?.children.orEmpty().count { child -> child is RoundRect }

fun QuestAnnouncementView.isTargetTicked(): Boolean = targetTick != null

/** A button in the place of the toast, to add under it. */
fun Container.buttonUnderTheToast(onConfirmed: () -> Unit): ConfirmButton = ConfirmButton("Confirm", 200.0, onConfirmed = onConfirmed).addTo(this).xy(100.0, 70.0)
