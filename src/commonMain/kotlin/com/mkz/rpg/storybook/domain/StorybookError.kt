package com.mkz.rpg.storybook.domain

sealed class StorybookError(
    message: String,
) : Throwable(message = message) {
    class EmptyCatalogue : StorybookError("The catalogue has no component")

    class ComponentWithoutStories(
        componentId: String,
    ) : StorybookError("The component '$componentId' has no story")

    class InvalidComponentId(
        componentId: String,
    ) : StorybookError("The component id '$componentId' is not in kebab case, e.g.: screen-health-bar")

    class InvalidStoryId(
        storyId: String,
    ) : StorybookError("The story id '$storyId' is not its component id and its name in kebab case, e.g.: screen-health-bar--full")

    class RepeatedComponentId(
        componentId: String,
    ) : StorybookError("The component id '$componentId' is repeated")

    class RepeatedStoryId(
        storyId: String,
    ) : StorybookError("The story id '$storyId' is repeated")

    class StoryNotFound(
        storyId: String,
    ) : StorybookError("The story '$storyId' is not in the catalogue")
}
