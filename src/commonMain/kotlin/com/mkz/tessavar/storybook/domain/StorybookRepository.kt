package com.mkz.tessavar.storybook.domain

interface StorybookRepository {
    fun save(storybook: Storybook)

    fun search(): Storybook?
}
