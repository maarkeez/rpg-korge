package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto

// Records the controls and args the presenter asks to display, instead of drawing them.
class FakeControlsView : ControlsView() {
    val displayedArgs = mutableListOf<Map<String, ValueDto>>()
    var displayedControls: List<ControlDto> = emptyList()
        private set
    var delegate: Delegate? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(
        controls: List<ControlDto>,
        args: Map<String, ValueDto>,
    ) {
        displayedControls = controls
        displayedArgs += args
    }
}
