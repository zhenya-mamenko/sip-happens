package com.siphappens

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityIntegrationTest {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PreferenceManager.setCounter(context, 0)
        PreferenceManager.setMaximum(context, 4)
        PreferenceManager.setImageCode(context, 1)
        PreferenceManager.setPlaySounds(context, true)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }

    @Test
    fun testMainActivityLaunches() {
        onView(withId(R.id.counterText)).check(matches(isDisplayed()))
        onView(withId(R.id.maxInput)).check(matches(isDisplayed()))
        onView(withId(R.id.resetButton)).check(matches(isDisplayed()))
        onView(withId(R.id.currentImage)).check(matches(isDisplayed()))
    }

    @Test
    fun testCounterDisplaysInitialValue() {
        onView(withId(R.id.counterText)).check(matches(withText("0")))
    }

    @Test
    fun testMaxInputHasDefaultValue() {
        onView(withId(R.id.maxInput)).check(matches(withText("4")))
    }

    @Test
    fun testImageSelectionChangesSelectedState() {
        onView(withId(R.id.image2)).perform(click())
        onView(withId(R.id.image2)).check(matches(isSelected()))

        onView(withId(R.id.image3)).perform(click())
        onView(withId(R.id.image3)).check(matches(isSelected()))
    }

    @Test
    fun testMaxInputValidation() {
        onView(withId(R.id.maxInput))
            .perform(clearText())
            .perform(typeText("150"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.maxInput)).check(matches(withText("15")))
    }

    @Test
    fun testMaxInputAcceptsValidValue() {
        onView(withId(R.id.maxInput))
            .perform(clearText())
            .perform(typeText("10"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.maxInput)).check(matches(withText("10")))
    }

    @Test
    fun testResetButtonResetsCounter() {
        onView(withId(R.id.resetButton)).perform(click())
        onView(withId(R.id.counterText)).check(matches(withText("0")))
    }

    @Test
    fun testPlaySoundsCheckboxToggle() {
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isChecked()))
        onView(withId(R.id.playSoundsCheckbox)).perform(click())
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isNotChecked()))
        onView(withId(R.id.playSoundsCheckbox)).perform(click())
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isChecked()))
    }

    @Test
    fun testActivityRecreation() {
        onView(withId(R.id.image3)).perform(click())
        onView(withId(R.id.maxInput))
            .perform(clearText())
            .perform(typeText("15"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.playSoundsCheckbox)).perform(click())

        activityScenario.recreate()

        onView(withId(R.id.image3)).check(matches(isSelected()))
        onView(withId(R.id.maxInput)).check(matches(withText("15")))
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isNotChecked()))
    }

    @Test
    fun testCompleteUserWorkflow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PreferenceManager.setCounter(context, 99)

        activityScenario.recreate()

        onView(withId(R.id.image2)).perform(click())
        onView(withId(R.id.image2)).check(matches(isSelected()))

        onView(withId(R.id.maxInput))
            .perform(clearText())
            .perform(typeText("8"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.maxInput)).check(matches(withText("8")))

        onView(withId(R.id.counterText)).check(matches(withText("99")))
        onView(withId(R.id.resetButton)).perform(click())
        onView(withId(R.id.counterText)).check(matches(withText("0")))

        onView(withId(R.id.playSoundsCheckbox)).check(matches(isChecked()))
        onView(withId(R.id.playSoundsCheckbox)).perform(click())
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isNotChecked()))
    }
}
