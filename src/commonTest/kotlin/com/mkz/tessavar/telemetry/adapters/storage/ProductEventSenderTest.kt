package com.mkz.tessavar.telemetry.adapters.storage

import com.mkz.tessavar.telemetry.adapters.storage.ProductEventMother.productEventRecorded
import com.mkz.tessavar.telemetry.adapters.storage.RemoteTelemetryRepository.Delivery
import com.mkz.tessavar.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.service.storage.InmemoryStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductEventSenderTest {
    private val storage = InmemoryStorage()
    private val nativeStorageTelemetryRepository = NativeStorageTelemetryRepository(storage)
    private val remoteTelemetryRepository = RecordingRemoteTelemetryRepository()
    private val sender = ProductEventSender(nativeStorageTelemetryRepository, remoteTelemetryRepository, TestScope(UnconfinedTestDispatcher()))

    @Test
    fun `ProductEventSender should send the queue and empty it when the remote accepts it`() {
        // Given
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded(Name.MAIN_MENU_OPENED), productEventRecorded(Name.GAME_STARTED)))
        // When
        sender.send()
        // Then
        remoteTelemetryRepository.batches.shouldContainExactly(listOf(listOf(productEventRecorded(Name.MAIN_MENU_OPENED), productEventRecorded(Name.GAME_STARTED))))
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
        storage.keys().shouldBeEmpty()
    }

    @Test
    fun `ProductEventSender should keep the queue when the remote does not receive it`() {
        // Given
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded()))
        remoteTelemetryRepository.delivery = Delivery.NOT_RECEIVED
        // When
        sender.send()
        // Then
        remoteTelemetryRepository.batches.shouldContainExactly(listOf(listOf(productEventRecorded())))
        nativeStorageTelemetryRepository.searchQueued().shouldContainExactly(productEventRecorded())
    }

    @Test
    fun `ProductEventSender should send batches of 100 product events`() {
        // Given
        nativeStorageTelemetryRepository.queue(List(250) { index -> productEventRecorded(occurredAt = index.toDouble()) })
        // When
        sender.send()
        // Then
        remoteTelemetryRepository.batches.map(List<*>::size).shouldContainExactly(100, 100, 50)
        remoteTelemetryRepository.created.shouldContainExactly(List(250) { index -> productEventRecorded(occurredAt = index.toDouble()) })
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
    }

    @Test
    fun `ProductEventSender should drop a batch the remote rejects`() {
        // Given
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded(Name.ITEM_BOUGHT)))
        remoteTelemetryRepository.delivery = Delivery.REJECTED
        // When
        sender.send()
        // Then
        remoteTelemetryRepository.batches.shouldContainExactly(listOf(listOf(productEventRecorded(Name.ITEM_BOUGHT))))
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
    }

    @Test
    fun `ProductEventSender should send what was queued while a batch was in flight once it is done`() {
        // Given
        val answer = CompletableDeferred<Unit>()
        remoteTelemetryRepository.answer = answer
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded(Name.BATTLE_STARTED)))
        sender.send()
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded(Name.BATTLE_FINISHED)))
        sender.send()
        // When
        answer.complete(Unit)
        // Then
        remoteTelemetryRepository.batches.shouldContainExactly(
            listOf(productEventRecorded(Name.BATTLE_STARTED)),
            listOf(productEventRecorded(Name.BATTLE_FINISHED)),
        )
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
    }
}
