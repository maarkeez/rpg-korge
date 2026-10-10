package com.mkz.rpg.screen

/** Portrait battle screen layout, in points. The map fills the screen and the contextual sheet overlays it. */
object BattleLayout {
    const val SCREEN_WIDTH = 390
    const val SCREEN_HEIGHT = 844

    // TODO: apply real device safe-area insets. They are 0 until the app targets notched devices.
    const val SAFE_AREA_TOP = 0
    const val SAFE_AREA_BOTTOM = 0

    const val TOP_STRIP_HEIGHT = 44
    const val TOP_STRIP_PADDING = 8
    const val MIN_TOUCH_TARGET = 44

    /** Height of the bar holding End Turn (floating) or Cancel and Confirm. */
    const val ACTION_BAR_HEIGHT = 48
    const val FINISH_TURN_WIDTH = 150
    const val FINISH_TURN_MARGIN = 8

    /** Tall enough for the attack preview (the tallest content), and below the 260 pt target. */
    const val SHEET_HEIGHT = 230

    const val BATTLEFIELD_Y = SAFE_AREA_TOP + TOP_STRIP_HEIGHT
    const val BATTLEFIELD_HEIGHT = SCREEN_HEIGHT - BATTLEFIELD_Y
    const val ACTION_BAR_Y = SCREEN_HEIGHT - SAFE_AREA_BOTTOM - ACTION_BAR_HEIGHT
    const val SHEET_Y = ACTION_BAR_Y - SHEET_HEIGHT
}
