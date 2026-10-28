package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.storybook.adapters.page.StorybookPage
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookEvent.ActionRecorded
import com.mkz.rpg.storybook.domain.StorybookEvent.ActionsCleared
import com.mkz.rpg.storybook.domain.StorybookEvent.ArgChanged
import com.mkz.rpg.storybook.domain.StorybookEvent.ArgsReset
import com.mkz.rpg.storybook.domain.StorybookEvent.CatalogueInitialised
import com.mkz.rpg.storybook.domain.StorybookEvent.GlobalChanged
import com.mkz.rpg.storybook.domain.StorybookEvent.StorySelected

/**
 * Lists the catalogue in the sidebar, and shows the story on show with its args and globals, rebuilding its view from
 * scratch whenever any of them changes. The language global selects the language of the texts the views are built with.
 * The callbacks the view makes are logged in the actions, and change the arg they are mapped to, if any. The page is told
 * once the story has rendered, so a script can wait for it.
 */
class StorybookPresenter(
    private val sidebarView: SidebarView,
    private val toolbarView: ToolbarView,
    private val storyInfoView: StoryInfoView,
    private val controlsView: ControlsView,
    private val actionsView: ActionsView,
    private val phoneFrameView: PhoneFrameView,
    private val storybookApi: StorybookApi,
    private val storyContext: StoryContext,
    private val storybookPage: StorybookPage,
    eventBus: EventBus,
) {
    private val subscriptions =
        listOf(
            eventBus.subscribe<CatalogueInitialised> {
                sidebarView.display(storybookApi.searchCatalogue())
            },
            eventBus.subscribe<StorySelected> { displayStory() },
            eventBus.subscribe<ArgChanged> { displayStory() },
            eventBus.subscribe<ArgsReset> { displayStory() },
            eventBus.subscribe<GlobalChanged> { displayStory() },
            eventBus.subscribe<ActionRecorded> { displayActions() },
            eventBus.subscribe<ActionsCleared> { displayActions() },
        )

    init {
        sidebarView.setDelegate(
            object : SidebarView.Delegate {
                override fun storySelected(storyId: String) = storybookApi.selectStory(storyId)
            },
        )
        toolbarView.setDelegate(
            object : ToolbarView.Delegate {
                override fun languageSelected(languageCode: String) = storybookApi.changeGlobal(StorybookApi.LANGUAGE_GLOBAL, ArgDto.text(languageCode))
            },
        )
        controlsView.setDelegate(
            object : ControlsView.Delegate {
                override fun argChanged(
                    name: String,
                    value: ArgDto,
                ) = storybookApi.changeArg(name, value)

                override fun argsReset() = storybookApi.resetArgs()
            },
        )
        actionsView.setDelegate(
            object : ActionsView.Delegate {
                override fun actionsCleared() = storybookApi.clearActions()
            },
        )
        storyContext.setDelegate(
            object : StoryContext.Delegate {
                override fun actionCalled(
                    name: String,
                    arguments: List<String>,
                    setArg: Pair<String, ArgDto>?,
                ) {
                    storybookApi.recordAction(name, arguments)
                    setArg?.let { (argName, value) -> storybookApi.changeArg(argName, value) }
                }
            },
        )
    }

    private fun displayStory() {
        val story = storybookApi.searchSelectedStory() ?: return
        val component = storybookApi.searchCatalogue().first { component -> story in component.stories }
        val args = storybookApi.searchArgs()
        val languageCode = requireNotNull(storybookApi.searchGlobals().getValue(StorybookApi.LANGUAGE_GLOBAL).text)
        storyContext.texts.selectLanguage(languageCode)
        sidebarView.selectStory(story.id)
        toolbarView.display(storyContext.texts.languages(), languageCode)
        storyInfoView.display(component, story)
        controlsView.display(component.controls, args)
        displayActions()
        phoneFrameView.display(storybookApi.bindingOf(component.id).render(Args(args), storyContext))
        storybookPage.storyRendered(story.id)
    }

    private fun displayActions() {
        actionsView.display(storybookApi.searchActions())
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
