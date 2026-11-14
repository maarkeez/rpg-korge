package com.mkz.tessavar.player.adapters.resources

import com.mkz.tessavar.player.domain.Player
import com.mkz.tessavar.player.usecases.services.PlayerLoader
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.enumOf
import com.mkz.tessavar.shared.adapters.toml.fromResource
import com.mkz.tessavar.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

class ResourcesPlayerLoader(
    private val playerRoot: VfsFile = ResourcesBundle.root["player"],
) : PlayerLoader {
    private var players = emptyList<Player.Dto>()

    override suspend fun initResources() {
        players =
            playerRoot.readTomlFiles<PlayerDefinition>().map { (playerId, definition) ->
                fromResource("player/$playerId.toml") { definition.toDto(playerId) }
            }
    }

    override fun loadPlayers(): List<Player.Dto> = players

    @Serializable
    data class PlayerDefinition(
        val type: String,
    ) {
        fun toDto(playerId: String): Player.Dto =
            Player.Dto(
                id = playerId,
                type = enumOf<Player.Dto.PlayerTypeDto>("type", type),
            )
    }
}
