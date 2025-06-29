package com.siphappens

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ImageHelperTest::class,
    PreferenceManagerTest::class
)
class UnitTestSuite
