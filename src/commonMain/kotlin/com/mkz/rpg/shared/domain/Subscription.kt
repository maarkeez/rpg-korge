package com.mkz.rpg.shared.domain

class Subscription(
    private val disposeAction: () -> Unit,
) {
    fun dispose() {
        disposeAction()
    }
}
