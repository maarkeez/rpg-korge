package com.mkz.rpg.shared.domain

/**
 * The runtime state of a subdomain, taken to save the game and restored to load it, e.g.: the health of each battle
 * unit. Its snapshot is the subdomain's own text (e.g.: JSON), so whoever saves it never knows its shape. Restoring
 * replaces the whole state, publishing no events, and fails when the snapshot can not be restored (e.g.: it names a
 * unit the game no longer has).
 */
interface Snapshottable {
    /** The name the snapshot is saved under, unique per subdomain, e.g.: `battleUnit`. */
    val key: String

    fun snapshot(): String

    fun restore(snapshot: String)
}
