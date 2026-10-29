package com.mkz.rpg.screen.adapters.presentation

/** Where a tile of the battlefield is, as the views of the battlefield draw it: its row and its column, from 0. */
data class TilePosition(
    val row: Int,
    val column: Int,
)
