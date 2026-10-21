package com.mkz.rpg.player.adapters.resources

import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.services.PlayerLoader
import com.mkz.rpg.shared.adapters.resources.ResourcesBundle
import com.mkz.rpg.shared.adapters.toml.enumOf
import com.mkz.rpg.shared.adapters.toml.fromResource
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

/** Loads one player per `player/<player id>.toml` resource. */
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
        val name: String,
        val type: String,
    ) {
        fun toDto(playerId: String): Player.Dto =
            Player.Dto(
                id = playerId,
                name = name,
                type = enumOf<Player.Dto.PlayerTypeDto>("type", type),
            )
    }
}
