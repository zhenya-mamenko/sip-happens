package com.mamenko.siphappens

import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import java.io.IOException

class CustomSoundStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val context = mock(Context::class.java)
    private val resolver = mock(ContentResolver::class.java)
    private val uri = mock(Uri::class.java)
    private val soundBytes = byteArrayOf(1, 2, 3)

    @Before
    fun setup() {
        `when`(context.filesDir).thenReturn(temporaryFolder.root)
        `when`(context.contentResolver).thenReturn(resolver)
        `when`(resolver.openInputStream(uri)).thenAnswer { ByteArrayInputStream(soundBytes) }
    }

    @Test
    fun `duration accepts positive values up to exactly three seconds`() {
        listOf(1, 2_999, 3_000).forEach(CustomSoundStore::requireValidDuration)
    }

    @Test
    fun `duration rejects empty unknown and over-limit sounds`() {
        listOf(-1, 0, 3_001, Int.MAX_VALUE).forEach { duration ->
            assertThrows(IllegalArgumentException::class.java) {
                CustomSoundStore.requireValidDuration(duration)
            }
        }
    }

    @Test
    fun `valid selection is copied and each sound is stored independently`() {
        mockConstruction(MediaPlayer::class.java) { player, _ ->
            `when`(player.duration).thenReturn(3_000)
        }.use { players ->
            CustomSoundStore.importSound(context, SoundType.BEFORE_MAX, uri)
            assertTrue(CustomSoundStore.hasCustomSound(context, SoundType.BEFORE_MAX))
            assertFalse(CustomSoundStore.hasCustomSound(context, SoundType.AFTER_MAX))
            assertArrayEquals(
                soundBytes, CustomSoundStore.getFile(context, SoundType.BEFORE_MAX).readBytes()
            )
            verify(players.constructed().single()).prepare()
            verify(players.constructed().single()).release()
        }
    }

    @Test
    fun `valid selection replaces a previous sound without leaving temporary files`() {
        val destination = CustomSoundStore.getFile(context, SoundType.BEFORE_MAX)
        destination.parentFile!!.mkdirs()
        destination.writeText("old sound")
        mockConstruction(MediaPlayer::class.java) { player, _ ->
            `when`(player.duration).thenReturn(1_000)
        }.use {
            CustomSoundStore.importSound(context, SoundType.BEFORE_MAX, uri)
        }
        assertArrayEquals(soundBytes, destination.readBytes())
        assertEquals(listOf(destination.name), destination.parentFile!!.list()!!.toList())
    }

    @Test
    fun `too long selection preserves current sound and removes temporary copy`() {
        val destination = CustomSoundStore.getFile(context, SoundType.AFTER_MAX)
        destination.parentFile!!.mkdirs()
        destination.writeText("previous sound")
        mockConstruction(MediaPlayer::class.java) { player, _ ->
            `when`(player.duration).thenReturn(3_001)
        }.use { players ->
            assertThrows(IllegalArgumentException::class.java) {
                CustomSoundStore.importSound(context, SoundType.AFTER_MAX, uri)
            }
            verify(players.constructed().single()).release()
        }
        assertEquals("previous sound", destination.readText())
        assertEquals(listOf(destination.name), destination.parentFile!!.list()!!.toList())
    }

    @Test
    fun `unplayable selection is rejected and player is released`() {
        mockConstruction(MediaPlayer::class.java) { player, _ ->
            doThrow(IOException("Unsupported audio")).`when`(player).prepare()
        }.use { players ->
            assertThrows(IOException::class.java) {
                CustomSoundStore.importSound(context, SoundType.BEFORE_MAX, uri)
            }
            verify(players.constructed().single()).release()
        }
        val destination = CustomSoundStore.getFile(context, SoundType.BEFORE_MAX)
        assertFalse(destination.exists())
        assertTrue(destination.parentFile!!.list()!!.isEmpty())
    }

    @Test
    fun `missing input is rejected without saving a sound`() {
        `when`(resolver.openInputStream(uri)).thenReturn(null)
        assertThrows(IOException::class.java) {
            CustomSoundStore.importSound(context, SoundType.BEFORE_MAX, uri)
        }
        assertFalse(CustomSoundStore.hasCustomSound(context, SoundType.BEFORE_MAX))
    }

    @Test
    fun `reset restores only the selected default and can be repeated`() {
        SoundType.entries.forEach { type ->
            val file = CustomSoundStore.getFile(context, type)
            file.parentFile!!.mkdirs()
            file.writeText("custom sound")
        }
        CustomSoundStore.resetSound(context, SoundType.BEFORE_MAX)
        CustomSoundStore.resetSound(context, SoundType.BEFORE_MAX)
        assertFalse(CustomSoundStore.hasCustomSound(context, SoundType.BEFORE_MAX))
        assertTrue(CustomSoundStore.hasCustomSound(context, SoundType.AFTER_MAX))
    }
}
