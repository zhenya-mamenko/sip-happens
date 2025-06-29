package com.siphappens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class SipHappensWidgetTest {

    private lateinit var context: Context
    private lateinit var widget: SipHappensWidget

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        widget = SipHappensWidget()
    }

    @Test
    fun testWidgetExists() {
        val componentName = ComponentName(context, SipHappensWidget::class.java)
        assertTrue("Widget component should exist", componentName.className.isNotEmpty())
    }

    @Test
    fun testWidgetClickIncrementsCounter() {
        PreferenceManager.setCounter(context, 0)
        PreferenceManager.setMaximum(context, 5)

        val intent = Intent(context, SipHappensWidget::class.java).apply {
            action = "com.siphappens.ACTION_WIDGET_CLICK"
        }

        widget.onReceive(context, intent)

        assertEquals(1, PreferenceManager.getCounter(context))
    }

    @Test
    fun testWidgetClickWhenCounterAtMaximum() {

        val maximum = 3
        PreferenceManager.setCounter(context, maximum - 1)
        PreferenceManager.setMaximum(context, maximum)

        val intent = Intent(context, SipHappensWidget::class.java).apply {
            action = "com.siphappens.ACTION_WIDGET_CLICK"
        }

        widget.onReceive(context, intent)

        assertEquals(maximum, PreferenceManager.getCounter(context))
    }

    @Test
    fun testWidgetClickBeyondMaximum() {
        val maximum = 4
        PreferenceManager.setCounter(context, maximum)
        PreferenceManager.setMaximum(context, maximum)

        val intent = Intent(context, SipHappensWidget::class.java).apply {
            action = "com.siphappens.ACTION_WIDGET_CLICK"
        }

        widget.onReceive(context, intent)

        assertEquals(maximum + 1, PreferenceManager.getCounter(context))
    }

    @Test
    fun testWidgetUpdateWithDifferentImageCodes() {
        val testCases = listOf(1, 2, 3)

        testCases.forEach { imageCode ->
            PreferenceManager.setImageCode(context, imageCode)
            PreferenceManager.setCounter(context, 2)
            PreferenceManager.setMaximum(context, 4)

            SipHappensWidget.updateWidget(context)

            assertEquals(imageCode, PreferenceManager.getImageCode(context))
        }
    }

    @Test
    fun testWidgetOnEnabled() {
        PreferenceManager.setCounter(context, 1)
        PreferenceManager.setMaximum(context, 5)
        PreferenceManager.setImageCode(context, 2)

        widget.onEnabled(context)

        assertEquals(1, PreferenceManager.getCounter(context))
        assertEquals(5, PreferenceManager.getMaximum(context))
        assertEquals(2, PreferenceManager.getImageCode(context))
    }

    @Test
    fun testMultipleWidgetClicks() {
        PreferenceManager.setCounter(context, 0)
        PreferenceManager.setMaximum(context, 10)

        val intent = Intent(context, SipHappensWidget::class.java).apply {
            action = "com.siphappens.ACTION_WIDGET_CLICK"
        }

        repeat(5) {
            widget.onReceive(context, intent)
        }

        assertEquals(5, PreferenceManager.getCounter(context))
    }

    @Test
    fun testWidgetIgnoresWrongAction() {
        val initialCounter = 3
        PreferenceManager.setCounter(context, initialCounter)

        val intent = Intent(context, SipHappensWidget::class.java).apply {
            action = "wrong.action"
        }

        widget.onReceive(context, intent)

        assertEquals(initialCounter, PreferenceManager.getCounter(context))
    }
}
