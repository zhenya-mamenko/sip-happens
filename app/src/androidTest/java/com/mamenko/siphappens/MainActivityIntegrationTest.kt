package com.mamenko.siphappens

import android.view.View
import android.os.SystemClock
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        SoundType.entries.forEach { CustomSoundStore.resetSound(context, it) }

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
    fun testCounterTextClickIncrementsCounter() {
        onView(withId(R.id.counterText)).perform(click())

        onView(withId(R.id.counterText)).check(matches(withText("1")))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(1, PreferenceManager.getCounter(context))
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
        onView(withId(R.id.resetButton)).perform(scrollTo(), click())
        onView(withId(R.id.counterText)).check(matches(withText("0")))
    }

    @Test
    fun testPlaySoundsCheckboxToggle() {
        onView(withId(R.id.playSoundsCheckbox)).perform(scrollTo())
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isChecked()))
        onView(withId(R.id.playSoundsCheckbox)).perform(scrollTo(), click())
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isNotChecked()))
        onView(withId(R.id.playSoundsCheckbox)).perform(scrollTo(), click())
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isChecked()))
    }

    @Test
    fun testActivityRecreation() {
        onView(withId(R.id.image3)).perform(click())
        onView(withId(R.id.maxInput))
            .perform(clearText())
            .perform(typeText("15"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.playSoundsCheckbox)).perform(scrollTo(), click())

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
        onView(withId(R.id.resetButton)).perform(scrollTo(), click())
        onView(withId(R.id.counterText)).check(matches(withText("0")))

        onView(withId(R.id.playSoundsCheckbox)).check(matches(isChecked()))
        onView(withId(R.id.playSoundsCheckbox)).perform(scrollTo(), click())
        onView(withId(R.id.playSoundsCheckbox)).check(matches(isNotChecked()))
    }

    @Test
    fun testOptionsAccordionAndDefaults() {
        onView(withId(R.id.optionsContent))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.optionsHeader)).perform(scrollTo(), click())
        onView(withId(R.id.optionsContent))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.beforeSoundStatus)).check(matches(withText(R.string.default_sound)))
        onView(withId(R.id.afterSoundStatus)).check(matches(withText(R.string.default_sound)))
        onView(withId(R.id.resetBeforeSound)).check(matches(isNotEnabled()))
        onView(withId(R.id.resetAfterSound)).check(matches(isNotEnabled()))

        activityScenario.recreate()

        onView(withId(R.id.optionsContent))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.optionsHeader)).perform(scrollTo(), click())
        onView(withId(R.id.optionsContent))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testShortViewportScrollsToAllSoundOptions() {
        activityScenario.onActivity { activity ->
            val scroll = activity.findViewById<ScrollView>(R.id.contentScroll)
            val params = scroll.layoutParams as LinearLayout.LayoutParams
            params.height = (240 * activity.resources.displayMetrics.density).toInt()
            params.weight = 0f
            scroll.layoutParams = params
        }
        onView(withId(R.id.optionsHeader)).perform(scrollTo(), click())
        onView(withId(R.id.chooseAfterSound)).perform(scrollTo()).check(matches(isDisplayed()))
        activityScenario.onActivity { activity ->
            val scroll = activity.findViewById<ScrollView>(R.id.contentScroll)
            assertTrue(scroll.getChildAt(0).height > scroll.height)
            assertTrue(scroll.scrollY > 0)
            assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.toolbar).visibility)
        }
    }

    @Test
    fun testCustomSoundStatusPersistsAndEachDefaultCanBeRestored() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SoundType.entries.forEach { type ->
            val file = CustomSoundStore.getFile(context, type)
            file.parentFile!!.mkdirs()
            context.resources.openRawResource(type.defaultResource).use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        activityScenario.recreate()
        onView(withId(R.id.optionsHeader)).perform(scrollTo(), click())
        onView(withId(R.id.beforeSoundStatus)).check(matches(withText(R.string.custom_sound)))
        onView(withId(R.id.afterSoundStatus)).check(matches(withText(R.string.custom_sound)))

        onView(withId(R.id.resetBeforeSound)).perform(scrollTo(), click())
        waitForDefaultSoundStatus(R.id.beforeSoundStatus)
        onView(withId(R.id.afterSoundStatus)).check(matches(withText(R.string.custom_sound)))
        onView(withId(R.id.resetBeforeSound)).check(matches(isNotEnabled()))

        onView(withId(R.id.resetAfterSound)).perform(scrollTo(), click())
        waitForDefaultSoundStatus(R.id.afterSoundStatus)
        onView(withId(R.id.resetAfterSound)).check(matches(isNotEnabled()))
        assertTrue(SoundType.entries.none { CustomSoundStore.hasCustomSound(context, it) })
    }

    private fun waitForDefaultSoundStatus(viewId: Int) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        var restored = false
        while (!restored && SystemClock.uptimeMillis() < deadline) {
            activityScenario.onActivity { activity ->
                restored = activity.findViewById<TextView>(viewId).text ==
                    activity.getString(R.string.default_sound)
            }
            if (!restored) SystemClock.sleep(20)
        }
        assertTrue("Default sound status was not restored", restored)
    }
}
