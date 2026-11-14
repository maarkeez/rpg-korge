package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.telemetry.adapters.presentation.TelemetryApi
import com.mkz.tessavar.telemetry.adapters.storage.RemoteTelemetryRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import korlibs.io.net.http.FakeHttpClient
import korlibs.korge.service.storage.InmemoryStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.random.Random
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentPresenterTest {
    private val storage = InmemoryStorage()
    private val messages = MessagesMother.messages()
    private val consentView = ConsentView(messages)
    private val privacyNoticeView = PrivacyNoticeView(messages)

    private fun telemetryApi() = TelemetryApi(storage, RemoteTelemetryRepository(collectorUrl = null, httpClient = FakeHttpClient()), TestScope(UnconfinedTestDispatcher()), Random(1)) { 1_000.0 }

    private fun presenter() = ConsentPresenter(consentView, privacyNoticeView, telemetryApi())

    @Test
    fun `ConsentPresenter should show the consent dialog when the game is launched for the first time`() {
        // When
        presenter()
        // Then
        consentView.visible.shouldBeTrue()
        privacyNoticeView.visible.shouldBeFalse()
    }

    @Test
    fun `ConsentPresenter should not show the consent dialog when it was answered in an earlier launch`() {
        // Given
        presenter().consentDeclined()
        // When
        presenter()
        // Then
        consentView.visible.shouldBeFalse()
    }

    @Test
    fun `ConsentPresenter should keep an install id on the device and hide the dialog when Accept is tapped`() {
        // Given
        val presenter = presenter()
        // When
        presenter.consentAccepted()
        // Then
        consentView.visible.shouldBeFalse()
        storage.getOrNull("telemetry.installId").shouldNotBeNull()
    }

    @Test
    fun `ConsentPresenter should delete the install id and hide the dialog when Decline is tapped from Privacy`() {
        // Given
        val presenter = presenter()
        presenter.consentAccepted()
        presenter.open()
        // When
        presenter.consentDeclined()
        // Then
        consentView.visible.shouldBeFalse()
        storage.getOrNull("telemetry.installId").shouldBeNull()
        storage.getOrNull("telemetry.consent").shouldNotBeNull() shouldContain "DECLINED"
    }

    @Test
    fun `ConsentPresenter should show the consent dialog when Privacy is tapped`() {
        // Given
        val presenter = presenter()
        presenter.consentAccepted()
        // When
        presenter.open()
        // Then
        consentView.visible.shouldBeTrue()
    }

    @Test
    fun `ConsentPresenter should show the privacy notice when Read more is tapped`() {
        // Given
        val presenter = presenter()
        // When
        presenter.consentReadMoreSelected()
        // Then
        privacyNoticeView.visible.shouldBeTrue()
        consentView.visible.shouldBeFalse()
    }

    @Test
    fun `ConsentPresenter should show the consent dialog again when Back is tapped on the privacy notice`() {
        // Given
        val presenter = presenter()
        presenter.consentReadMoreSelected()
        // When
        presenter.privacyNoticeBackSelected()
        // Then
        consentView.visible.shouldBeTrue()
        privacyNoticeView.visible.shouldBeFalse()
    }
}
