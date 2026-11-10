package com.mkz.rpg.shared.usecases.acceptance

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

fun scenarioTest(
    setUp: suspend () -> Unit,
    test: suspend () -> Unit,
): TestResult =
    runTest {
        setUp()
        test()
    }
