package com.mkz.tessavar.storybook.adapters.storage

import com.mkz.tessavar.storybook.domain.Storybook
import com.mkz.tessavar.storybook.domain.StorybookRepository

class InMemoryStorybookRepository : StorybookRepository {
    private var storybook: Storybook? = null

    override fun save(storybook: Storybook) {
        this.storybook = storybook
    }

    override fun search() = storybook
}
