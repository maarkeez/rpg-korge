package com.mkz.rpg.npc.adapters.resources

import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.npc.usecases.services.NpcLoader
import com.mkz.rpg.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.Serializable

/** Loads one npc per `npc/<npc id>.toml` resource. */
class ResourcesNpcLoader(
    private val npcRoot: VfsFile = resourcesVfs["npc"],
) : NpcLoader {
    private var npcs = emptyList<Npc.Dto>()

    override suspend fun initResources() {
        npcs =
            npcRoot.readTomlFiles<NpcDefinition>().map { (npcId, definition) ->
                definition.toDto(npcId)
            }
    }

    override fun loadNpcs(): List<Npc.Dto> = npcs

    @Serializable
    data class NpcDefinition(
        val name: String,
        val line: String,
        val row: Int,
        val column: Int,
    ) {
        fun toDto(npcId: String): Npc.Dto =
            Npc.Dto(
                id = npcId,
                name = name,
                line = line,
                row = row,
                column = column,
            )
    }
}
