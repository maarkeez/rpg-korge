package com.mkz.rpg.poc.services

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FooTest :
    StringSpec({

        "foo should return the value of bar plus 2" {
            val bar =
                mock<Bar> {
                    every { bar() } returns 1
                }

            val foo = Foo(bar)

            foo.foo() shouldBe 3
        }
    })
