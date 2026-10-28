package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.MapBuilderCallToActionView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [MapBuilderCallToActionView], with the texts of the language global. As in the map builder, going back
 * asks to confirm leaving, and staying brings back the export, through its args.
 */
object MapBuilderCallToActionViewStories : StoryBinding {
    private const val CALL_TO_ACTION = "callToAction"

    /** What the view asks the player. */
    private enum class CallToAction { EXPORT_AND_BACK, LEAVE_CONFIRMATION }

    private val exportAndBack =
        StoryDto(
            id = "screen-map-builder-call-to-action--export-and-back",
            name = "Export and back",
            description = "Back on the left and Export on the right. Back asks to confirm leaving.",
            args = emptyMap(),
        )
    private val leaveConfirmation =
        StoryDto(
            id = "screen-map-builder-call-to-action--leave-confirmation",
            name = "Leave confirmation",
            description = "Stay on the left and Leave on the right: leaving loses the map. Stay brings back the export.",
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.LEAVE_CONFIRMATION.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-map-builder-call-to-action",
            group = "Screen",
            title = "Map builder call to action",
            description = "Under the map builder controls: exporting the map or going back to the menu, and confirming leaving, as the map is lost.",
            controls = listOf(ControlDto.enum(CALL_TO_ACTION, CallToAction.entries.map { callToAction -> callToAction.name })),
            args = mapOf(CALL_TO_ACTION to ArgDto.text(CallToAction.EXPORT_AND_BACK.name)),
            stories = listOf(exportAndBack, leaveConfirmation),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        MapBuilderCallToActionView(context.texts).apply {
            when (CallToAction.valueOf(args.text(CALL_TO_ACTION))) {
                CallToAction.EXPORT_AND_BACK ->
                    displayExportAndBack(
                        onExported = { context.action("onExported") },
                        onBack = { context.action("onBack", setArg = CALL_TO_ACTION to ArgDto.text(CallToAction.LEAVE_CONFIRMATION.name)) },
                    )
                CallToAction.LEAVE_CONFIRMATION ->
                    displayLeaveConfirmation(
                        onLeft = { context.action("onLeft") },
                        onStayed = { context.action("onStayed", setArg = CALL_TO_ACTION to ArgDto.text(CallToAction.EXPORT_AND_BACK.name)) },
                    )
            }
        }
}
