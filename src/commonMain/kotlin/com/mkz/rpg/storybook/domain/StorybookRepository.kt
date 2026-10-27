package com.mkz.rpg.storybook.domain

interface StorybookRepository {
    fun save(storybook: Storybook)

    fun search(): Storybook?
}
