package com.mkz.rpg.shared.usecases.acceptance

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

// Runs the acceptance test once its scenario has been set up. Setting up the scenario loads the resources, which suspends,
// so it can not be done in a `@BeforeTest`.
fun scenarioTest(
    setUp: suspend () -> Unit,
    test: suspend () -> Unit,
): TestResult =
    runTest {
        setUp()
        test()
    }
