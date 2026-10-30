package com.mkz.rpg.save.domain

import com.mkz.rpg.shared.domain.Snapshottable

/** A subdomain whose state is the [snapshot] text, which records the snapshots restored, in order, into [restored]. */
class FakeSnapshottable(
    override val key: String,
    var state: String = "{\"$key\":1}",
    private val restored: MutableList<String> = mutableListOf(),
    private val failure: Throwable? = null,
) : Snapshottable {
    override fun snapshot(): String = state

    override fun restore(snapshot: String) {
        failure?.let { throw it }
        state = snapshot
        restored += key
    }
}
