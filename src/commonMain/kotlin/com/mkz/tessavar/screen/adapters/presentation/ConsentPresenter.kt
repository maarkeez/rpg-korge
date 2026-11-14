package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.telemetry.adapters.presentation.TelemetryApi

class ConsentPresenter(
    private val consentView: ConsentView,
    private val privacyNoticeView: PrivacyNoticeView,
    private val telemetryApi: TelemetryApi,
) : ConsentView.Delegate,
    PrivacyNoticeView.Delegate {
    init {
        consentView.visible = false
        privacyNoticeView.visible = false
        consentView.setDelegate(this)
        privacyNoticeView.setDelegate(this)
        if (telemetryApi.searchConsent()) open()
    }

    fun open() {
        privacyNoticeView.visible = false
        consentView.display()
    }

    override fun consentAccepted() {
        telemetryApi.giveConsent()
        consentView.visible = false
    }

    override fun consentDeclined() {
        telemetryApi.declineConsent()
        consentView.visible = false
    }

    override fun consentReadMoreSelected() {
        consentView.visible = false
        privacyNoticeView.display()
    }

    override fun privacyNoticeBackSelected() = open()
}
