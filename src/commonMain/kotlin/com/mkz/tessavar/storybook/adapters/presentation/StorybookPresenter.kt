package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.Subscription
import com.mkz.tessavar.shared.domain.subscribe
import com.mkz.tessavar.storybook.adapters.page.StorybookPage
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import com.mkz.tessavar.storybook.domain.StorybookEvent.ActionRecorded
import com.mkz.tessavar.storybook.domain.StorybookEvent.ActionsCleared
import com.mkz.tessavar.storybook.domain.StorybookEvent.ArgChanged
import com.mkz.tessavar.storybook.domain.StorybookEvent.ArgsReset
import com.mkz.tessavar.storybook.domain.StorybookEvent.CatalogueOpened
import com.mkz.tessavar.storybook.domain.StorybookEvent.GlobalChanged
import com.mkz.tessavar.storybook.domain.StorybookEvent.StorySelected

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
            eventBus.subscribe<CatalogueOpened> {
                sidebarView.display(storybookApi.searchCatalogue())
            },
            eventBus.subscribe<StorySelected> { displayStory() },
            eventBus.subscribe<ArgChanged> { displayStory() },
            eventBus.subscribe<ArgsReset> { displayStory() },
            eventBus.subscribe<GlobalChanged> { displayStory() },
            eventBus.subscribe<ActionRecorded> { displayButtons() },
            eventBus.subscribe<ActionsCleared> { displayButtons() },
        )

    init {
        sidebarView.setDelegate(
            object : SidebarView.Delegate {
                override fun storySelected(storyId: String) = storybookApi.selectStory(storyId)
            },
        )
        toolbarView.setDelegate(
            object : ToolbarView.Delegate {
                override fun languageSelected(languageCode: String) = storybookApi.changeGlobal(StorybookApi.LANGUAGE_GLOBAL, ValueDto.text(languageCode))
            },
        )
        controlsView.setDelegate(
            object : ControlsView.Delegate {
                override fun argChanged(
                    name: String,
                    value: ValueDto,
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
                    setArg: Pair<String, ValueDto>?,
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
        storyContext.messages.selectLanguage(languageCode)
        sidebarView.selectStory(story.id)
        toolbarView.display(storyContext.messages.languages(), languageCode)
        storyInfoView.display(component, story)
        controlsView.display(component.controls, args)
        displayButtons()
        phoneFrameView.display(storybookApi.bindingOf(component.id).render(Args(args), storyContext))
        storybookPage.storyRendered(story.id)
    }

    private fun displayButtons() {
        actionsView.display(storybookApi.searchActions())
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
