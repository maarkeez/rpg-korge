package com.mkz.rpg.poc.services

class Foo(
    private val bar: Bar,
) {
    fun foo(): Int = bar.bar() + 2
}
