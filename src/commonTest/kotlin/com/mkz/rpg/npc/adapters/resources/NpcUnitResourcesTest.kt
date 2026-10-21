package com.mkz.rpg.npc.adapters.resources

import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

// The game does not check the unit of a recruitable npc when it starts, so that npc does not depend on unit: this does.
class NpcUnitResourcesTest {
    @Test
    fun `should find the unit of every recruitable npc in the unit resources`() =
        runTest {
            // Given
            val npcLoader = ResourcesNpcLoader().also { loader -> loader.initResources() }
            val unitLoader = ResourcesUnitLoader().also { loader -> loader.initResources() }
            val recruitableNpcs = npcLoader.loadNpcs().filter { npc -> npc.unitId != null }
            // When
            val unitIds = unitLoader.loadUnits().map { unit -> unit.id }.toSet()
            // Then
            recruitableNpcs.shouldNotBeEmpty()
            recruitableNpcs.filter { npc -> npc.unitId !in unitIds }.map { npc -> "${npc.id} -> ${npc.unitId}" }.shouldBeEmpty()
        }
}
