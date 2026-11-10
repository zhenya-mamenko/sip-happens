package com.mamenko.siphappens

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(AndroidJUnit4::class)
class CustomSoundIntegrationTest {
    private lateinit var context: Context
    private val sourceFiles = mutableListOf<File>()

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        SoundType.entries.forEach { CustomSoundStore.resetSound(context, it) }
    }

    @After
    fun cleanup() {
        SoundType.entries.forEach { CustomSoundStore.resetSound(context, it) }
        sourceFiles.forEach { file -> check(file.delete()) }
    }

    @Test
    fun acceptsExactlyThreeSecondsAndKeepsAnIndependentCopy() {
        val source = createWav(3_000)
        val expected = source.readBytes()
        CustomSoundStore.importSound(context, SoundType.BEFORE_MAX, Uri.fromFile(source))
        source.writeText("source changed")

        assertArrayEquals(expected, CustomSoundStore.getFile(context, SoundType.BEFORE_MAX).readBytes())
        assertFalse(CustomSoundStore.hasCustomSound(context, SoundType.AFTER_MAX))
    }

    @Test
    fun rejects3001MillisecondsWithoutReplacingCurrentSound() {
        val accepted = createWav(1_000)
        val rejected = createWav(3_001)
        CustomSoundStore.importSound(context, SoundType.AFTER_MAX, Uri.fromFile(accepted))

        assertThrows(IllegalArgumentException::class.java) {
            CustomSoundStore.importSound(context, SoundType.AFTER_MAX, Uri.fromFile(rejected))
        }
        assertArrayEquals(
            accepted.readBytes(), CustomSoundStore.getFile(context, SoundType.AFTER_MAX).readBytes()
        )
    }

    @Test
    fun replacesCustomSoundAndRestoresDefaultsIndependently() {
        val first = createWav(1_000)
        val second = createWav(2_000)
        SoundType.entries.forEach { type ->
            CustomSoundStore.importSound(context, type, Uri.fromFile(first))
        }
        CustomSoundStore.importSound(context, SoundType.BEFORE_MAX, Uri.fromFile(second))
        assertArrayEquals(
            second.readBytes(), CustomSoundStore.getFile(context, SoundType.BEFORE_MAX).readBytes()
        )

        CustomSoundStore.resetSound(context, SoundType.BEFORE_MAX)
        assertFalse(CustomSoundStore.hasCustomSound(context, SoundType.BEFORE_MAX))
        assertTrue(CustomSoundStore.hasCustomSound(context, SoundType.AFTER_MAX))
        CustomSoundStore.resetSound(context, SoundType.AFTER_MAX)
        assertFalse(CustomSoundStore.hasCustomSound(context, SoundType.AFTER_MAX))
    }

    private fun createWav(durationMs: Int): File {
        val sampleRate = 8_000
        val dataSize = durationMs * sampleRate / 1_000 * 2
        val bytes = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        bytes.put("RIFF".toByteArray(Charsets.US_ASCII))
        bytes.putInt(36 + dataSize)
        bytes.put("WAVEfmt ".toByteArray(Charsets.US_ASCII))
        bytes.putInt(16)
        bytes.putShort(1.toShort())
        bytes.putShort(1.toShort())
        bytes.putInt(sampleRate)
        bytes.putInt(sampleRate * 2)
        bytes.putShort(2.toShort())
        bytes.putShort(16.toShort())
        bytes.put("data".toByteArray(Charsets.US_ASCII))
        bytes.putInt(dataSize)
        return File.createTempFile("test_sound_", ".wav", context.cacheDir).also { file ->
            file.writeBytes(bytes.array())
            sourceFiles.add(file)
        }
    }
}
