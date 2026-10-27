package com.mkz.rpg.storybook.adapters.storage

import com.mkz.rpg.storybook.domain.Storybook
import com.mkz.rpg.storybook.domain.StorybookRepository

class InMemoryStorybookRepository : StorybookRepository {
    private var storybook: Storybook? = null

    override fun save(storybook: Storybook) {
        this.storybook = storybook
    }

    override fun search() = storybook
}
