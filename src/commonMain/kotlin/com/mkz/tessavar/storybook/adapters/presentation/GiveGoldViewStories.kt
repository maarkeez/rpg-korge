package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.GiveGoldView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object GiveGoldViewStories : StoryBinding {
    private const val MAXIMUM_GOLD = 99_999
    private const val RECEIVERS_LIMIT = 3
    private const val GIVER_UNIT = "knight"
    private val RECEIVER_UNITS = listOf("viking", "herbalist", "hero")

    private const val AMOUNT = "amount"
    private const val GIVER_GOLD = "giverGold"
    private const val RECEIVER_GOLD = "receiverGold"
    private const val RECEIVERS = "receivers"
    private const val RECEIVER_INDEX = "receiverIndex"
    private const val GIVER_NAME = "giverName"
    private const val RECEIVER_NAME = "receiverName"

    private val default = StoryDto(id = "screen-give-gold--default", name = "Default", description = "The knight gives 25 of its 120 G to the only receiver: there are no arrows.", args = emptyMap())
    private val severalReceivers =
        StoryDto(
            id = "screen-give-gold--several-receivers",
            name = "Several receivers",
            description = "The arrows switch the receiver, disabled at each end of the receivers.",
            args = mapOf(RECEIVERS to ValueDto.int(RECEIVERS_LIMIT)),
        )
    private val limited =
        StoryDto(
            id = "screen-give-gold--limited",
            name = "Limited by the receiver",
            description = "A receiver with 99,990 G takes at most 9 G, however much the giver carries.",
            args = mapOf(AMOUNT to ValueDto.int(9), RECEIVER_GOLD to ValueDto.int(99_990)),
        )
    private val longNames =
        StoryDto(
            id = "screen-give-gold--long-names",
            name = "Long names",
            description = "Names longer than their column are drawn smaller.",
            args = mapOf(GIVER_NAME to ValueDto.text("Sir Reginald the Unbreakable of the North"), RECEIVER_NAME to ValueDto.text("Archmage Eleanor of the Silver Wind")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-give-gold",
            group = "Screen",
            title = "Give gold",
            description = "Takes the place of the inventory while exploring: the giver, the receiver, what each would carry and the amount on a slider. Its buttons are in the call to action.",
            controls =
                listOf(
                    ControlDto.int(AMOUNT, minimum = 1, maximum = MAXIMUM_GOLD),
                    ControlDto.int(GIVER_GOLD, minimum = 1, maximum = MAXIMUM_GOLD),
                    ControlDto.int(RECEIVER_GOLD, minimum = 0, maximum = MAXIMUM_GOLD - 1),
                    ControlDto.int(RECEIVERS, minimum = 1, maximum = RECEIVERS_LIMIT),
                    ControlDto.int(RECEIVER_INDEX, minimum = 0, maximum = RECEIVERS_LIMIT - 1),
                    ControlDto.text(GIVER_NAME),
                    ControlDto.text(RECEIVER_NAME),
                ),
            args =
                mapOf(
                    AMOUNT to ValueDto.int(25),
                    GIVER_GOLD to ValueDto.int(120),
                    RECEIVER_GOLD to ValueDto.int(100),
                    RECEIVERS to ValueDto.int(1),
                    RECEIVER_INDEX to ValueDto.int(0),
                    GIVER_NAME to ValueDto.text("Knight"),
                    RECEIVER_NAME to ValueDto.text("Viking"),
                ),
            stories = listOf(default, severalReceivers, limited, longNames),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val giverGold = args.int(GIVER_GOLD)
        val receiverGold = args.int(RECEIVER_GOLD)
        val maximum = minOf(giverGold, MAXIMUM_GOLD - receiverGold)
        val receivers = args.int(RECEIVERS)
        val receiverIndex = args.int(RECEIVER_INDEX).coerceAtMost(receivers - 1)
        return GiveGoldView(context.resourcesLoader, context.messages).apply {
            setDelegate(
                object : GiveGoldView.Delegate {
                    override fun amountChanged(amount: Int) = context.action("amountChanged", amount)

                    override fun amountSettled(amount: Int) = context.action("amountSettled", amount, setArg = AMOUNT to ValueDto.int(amount))

                    override fun receiverStepped(step: Int) = context.action("receiverStepped", step, setArg = RECEIVER_INDEX to ValueDto.int(receiverIndex + step))
                },
            )
            display(
                giver = GiveGoldView.Member(args.text(GIVER_NAME), GIVER_UNIT, giverGold),
                receiver = GiveGoldView.Member(args.text(RECEIVER_NAME), RECEIVER_UNITS[receiverIndex], receiverGold),
                amount = args.int(AMOUNT).coerceAtMost(maximum),
                maximum = maximum,
                canStepToPrevious = receiverIndex > 0,
                canStepToNext = receiverIndex < receivers - 1,
            )
        }
    }
}
