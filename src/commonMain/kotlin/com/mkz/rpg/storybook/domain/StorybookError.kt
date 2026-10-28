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

    class InvalidControl(
        controlName: String,
        reason: String,
    ) : StorybookError("The control '$controlName' is not valid: $reason")

    class RepeatedControl(
        controlName: String,
    ) : StorybookError("The control '$controlName' is repeated")

    class ControlNotFound(
        controlName: String,
    ) : StorybookError("There is no control '$controlName'")

    class MissingValue(
        controlName: String,
    ) : StorybookError("The control '$controlName' has no default value")

    class InvalidValue(
        controlName: String,
        value: Any?,
    ) : StorybookError("The control '$controlName' does not accept the value '$value'")
}
