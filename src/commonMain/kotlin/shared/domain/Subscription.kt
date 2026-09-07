package shared.domain

class Subscription(
    private val disposeAction: () -> Unit
) {
    fun dispose() {
        disposeAction()
    }
}
