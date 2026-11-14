package com.mkz.tessavar.npc.adapters.resources

import com.mkz.tessavar.npc.domain.Npc
import com.mkz.tessavar.npc.usecases.services.NpcLoader
import com.mkz.tessavar.shared.adapters.resources.ResourcesBundle
import com.mkz.tessavar.shared.adapters.toml.readTomlFiles
import korlibs.io.file.VfsFile
import kotlinx.serialization.Serializable

class ResourcesNpcLoader(
    private val npcRoot: VfsFile = ResourcesBundle.root["npc"],
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
        val row: Int,
        val column: Int,
        val unitId: String? = null,
        val stock: List<String> = emptyList(),
    ) {
        fun toDto(npcId: String): Npc.Dto =
            Npc.Dto(
                id = npcId,
                row = row,
                column = column,
                unitId = unitId,
                stock = stock,
            )
    }
}
