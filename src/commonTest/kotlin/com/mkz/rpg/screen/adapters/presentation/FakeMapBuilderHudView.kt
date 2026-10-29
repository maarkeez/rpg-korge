package com.mkz.rpg.screen.adapters.presentation

// Records what the presenter asks the map builder controls to display, instead of drawing them.
class FakeMapBuilderHudView : MapBuilderHudView(TextsMother.texts()) {
    var delegate: Delegate? = null
        private set
    var displayedSize: Pair<Int, Int>? = null
        private set
    var displayedAllowedSizes: IntRange? = null
        private set
    var displayedTerrains: List<TerrainOption> = emptyList()
        private set
    var selectedTerrainId: String? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun displaySize(
        rows: Int,
        columns: Int,
        allowedSizes: IntRange,
    ) {
        displayedSize = rows to columns
        displayedAllowedSizes = allowedSizes
    }

    override fun displayTerrains(
        terrains: List<TerrainOption>,
        selectedTerrainId: String,
    ) {
        displayedTerrains = terrains
        this.selectedTerrainId = selectedTerrainId
    }

    override fun selectTerrain(terrainId: String) {
        selectedTerrainId = terrainId
    }
}
