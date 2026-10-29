package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto

/**
 * The search of the sidebar. A story matches when every word of the text, ignoring case, is somewhere in its group, the
 * title of its component and its name, in any order: `shop sell` finds **Screen / Shop › Sell**, and `shop` every story
 * of **Screen / Shop**.
 */
object CatalogueSearch {
    private val SPACES = Regex("\\s+")

    /** The components with a story that matches [text], each with only the stories that match, in their order. All of them when [text] is blank. */
    fun search(
        components: List<ComponentDto>,
        text: String,
    ): List<ComponentDto> {
        val words =
            text
                .trim()
                .lowercase()
                .split(SPACES)
                .filter(String::isNotEmpty)
        if (words.isEmpty()) return components
        return components.mapNotNull { component ->
            val stories =
                component.stories.filter { story ->
                    val searched = "${component.group} ${component.title} ${story.name}".lowercase()
                    words.all { word -> word in searched }
                }
            if (stories.isEmpty()) null else component.copy(stories = stories)
        }
    }
}
