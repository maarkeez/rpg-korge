package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto

// Records the controls and args the presenter asks to display, instead of drawing them.
class FakeControlsView : ControlsView() {
    val displayedArgs = mutableListOf<Map<String, ArgDto>>()
    var displayedControls: List<ControlDto> = emptyList()
        private set
    var delegate: Delegate? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(
        controls: List<ControlDto>,
        args: Map<String, ArgDto>,
    ) {
        displayedControls = controls
        displayedArgs += args
    }
}
