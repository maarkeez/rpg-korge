package com.mkz.tessavar.shared.domain

class Subscription(
    private val disposeAction: () -> Unit,
) {
    fun dispose() {
        disposeAction()
    }
}
