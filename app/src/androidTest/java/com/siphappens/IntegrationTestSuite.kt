package com.siphappens

import org.junit.runner.RunWith
import org.junit.runners.Suite

@Suite.SuiteClasses(
    MainActivityIntegrationTest::class,
    SipHappensWidgetTest::class
)
class IntegrationTestSuite
