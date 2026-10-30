package com.mkz.rpg.shared.adapters.presentation

// Page object of [GameButton]. Also used by the page objects of the views made of game buttons.

/** Whether the button is displayed faded, as a disabled one: its label grey and its border dim. */
fun GameButton.isFaded(): Boolean = textColor == GameButton.DISABLED_LABEL && background.borderColor == GameButton.DISABLED_BORDER

fun GameButton.backgroundColorWhenDisabled() = bgColorDisabled
