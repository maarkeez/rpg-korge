package com.mkz.rpg.shared.adapters.presentation

fun GameButton.isFaded(): Boolean = textColor == GameButton.DISABLED_LABEL && background.borderColor == GameButton.DISABLED_BORDER

fun GameButton.backgroundColorWhenDisabled() = bgColorDisabled
