package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.addTo
import korlibs.korge.view.xy

fun QuestAnnouncementView.isAnnouncementShown(): Boolean = isShown()

fun QuestAnnouncementView.displayedTitle(): String? = titleText?.text

fun QuestAnnouncementView.displayedName(): String? = nameText?.text

fun QuestAnnouncementView.displayedTask(): String? = taskText?.text

fun QuestAnnouncementView.displayedGold(): List<String> =
    rewardView
        ?.descendants()
        .orEmpty()
        .filterIsInstance<Text>()
        .map { line -> line.text }

fun QuestAnnouncementView.displayedItemSlots(): Int = rewardView?.children.orEmpty().count { child -> child is RoundRect }

fun QuestAnnouncementView.isTargetTicked(): Boolean = targetTick != null

fun Container.buttonUnderTheAnnouncement(onConfirmed: () -> Unit): ConfirmButton = ConfirmButton("Confirm", 200.0, onConfirmed = onConfirmed).addTo(this).xy(100.0, 70.0)
