package com.mkz.rpg.shared.domain

interface Snapshottable {
    val key: String

    fun snapshot(): String

    fun load(snapshot: String)
}
