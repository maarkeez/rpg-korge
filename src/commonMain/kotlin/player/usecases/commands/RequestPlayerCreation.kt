package player.usecases.commands

import player.domain.Player
import player.domain.PlayerRepository
import shared.domain.EventBus

class RequestPlayerCreation(
    private val playerRepository: PlayerRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(id: String, name: String, type: PlayerType) {
        if(playerRepository.searchById(id) != null) return
        val (events, player) = when(type){
            PlayerType.CPU -> Player.createCpu(id, name)
            PlayerType.HUMAN -> Player.createHuman(id, name)
        }.pullEvents()
        playerRepository.create(player)
        eventBus.publish(events)
    }

    enum class PlayerType {
        CPU,
        HUMAN
    }
}
