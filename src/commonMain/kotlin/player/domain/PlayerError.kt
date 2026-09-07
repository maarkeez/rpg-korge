package player.domain

sealed class PlayerError(message: String) : Throwable(message=message) {
    class EmptyPlayerId : PlayerError(message="Player id can not be empty")
    class EmptyPlayerName : PlayerError(message="Player name can not be empty")
    class PlayerNameLongerThanExpected : PlayerError(message="Player name is longer than 50 characters")
}
