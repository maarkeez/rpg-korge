package com.mkz.tessavar.screen.adapters.presentation

class FakeWorldBuilderCallToActionView : WorldBuilderCallToActionView(MessagesMother.messages()) {
    sealed interface Actions {
        data class ExportAndBack(
            val onExported: () -> Unit,
            val onBack: () -> Unit,
        ) : Actions

        data class LeaveConfirmation(
            val onLeft: () -> Unit,
            val onStayed: () -> Unit,
        ) : Actions
    }

    var displayedActions: Actions? = null
        private set

    override fun displayExportAndBack(
        onExported: () -> Unit,
        onBack: () -> Unit,
    ) {
        displayedActions = Actions.ExportAndBack(onExported, onBack)
    }

    override fun displayLeaveConfirmation(
        onLeft: () -> Unit,
        onStayed: () -> Unit,
    ) {
        displayedActions = Actions.LeaveConfirmation(onLeft, onStayed)
    }
}
