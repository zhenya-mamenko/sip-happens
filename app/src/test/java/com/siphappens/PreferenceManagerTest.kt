package com.siphappens

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PreferenceManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var realContext: Context

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        realContext = RuntimeEnvironment.getApplication()

        `when`(mockContext.getSharedPreferences("SipHappensPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
    }

    @Test
    fun `getCounter should return default value 0`() {
        `when`(mockSharedPreferences.getInt("counter", 0)).thenReturn(0)

        val result = PreferenceManager.getCounter(mockContext)

        assertEquals(0, result)
        verify(mockSharedPreferences).getInt("counter", 0)
    }

    @Test
    fun `setCounter should save counter value`() {
        val counterValue = 5

        PreferenceManager.setCounter(mockContext, counterValue)

        verify(mockEditor).putInt("counter", counterValue)
        verify(mockEditor).apply()
    }

    @Test
    fun `getMaximum should return default value 4`() {
        `when`(mockSharedPreferences.getInt("maximum", 4)).thenReturn(4)

        val result = PreferenceManager.getMaximum(mockContext)

        assertEquals(4, result)
        verify(mockSharedPreferences).getInt("maximum", 4)
    }

    @Test
    fun `setMaximum should save maximum value`() {
        val maximumValue = 10

        PreferenceManager.setMaximum(mockContext, maximumValue)

        verify(mockEditor).putInt("maximum", maximumValue)
        verify(mockEditor).apply()
    }

    @Test
    fun `getImageCode should return default value 1`() {
        `when`(mockSharedPreferences.getInt("image_code", 1)).thenReturn(1)

        val result = PreferenceManager.getImageCode(mockContext)

        assertEquals(1, result)
        verify(mockSharedPreferences).getInt("image_code", 1)
    }

    @Test
    fun `setImageCode should save image code value`() {
        val imageCode = 3

        PreferenceManager.setImageCode(mockContext, imageCode)

        verify(mockEditor).putInt("image_code", imageCode)
        verify(mockEditor).apply()
    }

    @Test
    fun `getPlaySounds should return default value true`() {
        `when`(mockSharedPreferences.getBoolean("play_sounds", true)).thenReturn(true)

        val result = PreferenceManager.getPlaySounds(mockContext)

        assertTrue(result)
        verify(mockSharedPreferences).getBoolean("play_sounds", true)
    }

    @Test
    fun `setPlaySounds should save play sounds value`() {
        val playSounds = false

        PreferenceManager.setPlaySounds(mockContext, playSounds)

        verify(mockEditor).putBoolean("play_sounds", playSounds)
        verify(mockEditor).apply()
    }

    @Test
    fun `real context integration test - counter operations`() {
        val testCounter = 7

        PreferenceManager.setCounter(realContext, testCounter)
        val retrievedCounter = PreferenceManager.getCounter(realContext)

        assertEquals(testCounter, retrievedCounter)
    }

    @Test
    fun `real context integration test - all preferences work together`() {
        val testCounter = 3
        val testMaximum = 8
        val testImageCode = 2
        val testPlaySounds = false

        PreferenceManager.setCounter(realContext, testCounter)
        PreferenceManager.setMaximum(realContext, testMaximum)
        PreferenceManager.setImageCode(realContext, testImageCode)
        PreferenceManager.setPlaySounds(realContext, testPlaySounds)

        assertEquals(testCounter, PreferenceManager.getCounter(realContext))
        assertEquals(testMaximum, PreferenceManager.getMaximum(realContext))
        assertEquals(testImageCode, PreferenceManager.getImageCode(realContext))
        assertEquals(testPlaySounds, PreferenceManager.getPlaySounds(realContext))
    }
}
