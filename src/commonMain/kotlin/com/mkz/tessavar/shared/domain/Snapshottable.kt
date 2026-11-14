package com.mkz.tessavar.shared.domain

interface Snapshottable {
    val key: String

    fun snapshot(): String

    fun load(snapshot: String)
}
