package com.mkz.rpg.storybook.adapters.presentation

// Page object of [StoryInfoView].

fun StoryInfoView.componentLabel(): String = componentText.plainText

fun StoryInfoView.componentDescription(): String = componentDescriptionText.plainText

fun StoryInfoView.storyName(): String = storyNameText.plainText

fun StoryInfoView.storyDescription(): String = storyDescriptionText.plainText
