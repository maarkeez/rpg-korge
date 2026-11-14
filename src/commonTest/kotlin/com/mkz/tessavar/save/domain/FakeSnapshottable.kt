package com.mkz.tessavar.save.domain

import com.mkz.tessavar.shared.domain.Snapshottable

class FakeSnapshottable(
    override val key: String,
    var state: String = "{\"$key\":1}",
    private val loaded: MutableList<String> = mutableListOf(),
    private val failure: Throwable? = null,
) : Snapshottable {
    override fun snapshot(): String = state

    override fun load(snapshot: String) {
        failure?.let { throw it }
        state = snapshot
        loaded += key
    }
}
