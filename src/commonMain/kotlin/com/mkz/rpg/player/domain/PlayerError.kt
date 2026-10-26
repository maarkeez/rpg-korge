package com.mkz.rpg.player.domain

sealed class PlayerError(
    message: String,
) : Throwable(message = message) {
    class EmptyPlayerId : PlayerError(message = "Player id can not be empty")
}
