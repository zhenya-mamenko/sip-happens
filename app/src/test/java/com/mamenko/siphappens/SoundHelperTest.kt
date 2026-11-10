package com.mamenko.siphappens

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetFileDescriptor
import android.content.res.Resources
import android.media.MediaPlayer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.io.FileDescriptor

class SoundHelperTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val context = mock(Context::class.java)
    private val preferences = mock(SharedPreferences::class.java)
    private val resources = mock(Resources::class.java)
    private val descriptor = mock(AssetFileDescriptor::class.java)
    private val fileDescriptor = FileDescriptor()

    @Before
    fun setup() {
        `when`(context.filesDir).thenReturn(temporaryFolder.root)
        `when`(context.getSharedPreferences("SipHappensPrefs", Context.MODE_PRIVATE))
            .thenReturn(preferences)
        `when`(preferences.getBoolean("play_sounds", true)).thenReturn(true)
        `when`(context.resources).thenReturn(resources)
        `when`(descriptor.fileDescriptor).thenReturn(fileDescriptor)
        `when`(descriptor.startOffset).thenReturn(0L)
        `when`(descriptor.length).thenReturn(100L)
        SoundType.entries.forEach { type ->
            `when`(resources.openRawResourceFd(type.defaultResource)).thenReturn(descriptor)
        }
    }

    @Test
    fun `both sound paths use defaults when no custom sound exists`() {
        mockConstruction(MediaPlayer::class.java).use { players ->
            SoundHelper.playBeforeMaxSound(context)
            verify(resources).openRawResourceFd(R.raw.sound_before_max)
            SoundHelper.playAfterMaxSound(context)
            verify(resources).openRawResourceFd(R.raw.sound_after_max)
            verify(players.constructed().first()).release()
            players.constructed().forEach { player ->
                verify(player).setDataSource(fileDescriptor, 0L, 100L)
                verify(player).prepare()
                verify(player).start()
            }
            complete(players.constructed().last())
        }
    }

    @Test
    fun `each sound path plays its own saved file`() {
        SoundType.entries.forEach { type ->
            val file = CustomSoundStore.getFile(context, type)
            file.parentFile!!.mkdirs()
            file.writeText("custom")
        }
        mockConstruction(MediaPlayer::class.java).use { players ->
            SoundHelper.playBeforeMaxSound(context)
            SoundHelper.playAfterMaxSound(context)
            verify(players.constructed()[0]).setDataSource(
                CustomSoundStore.getFile(context, SoundType.BEFORE_MAX).absolutePath
            )
            verify(players.constructed()[1]).setDataSource(
                CustomSoundStore.getFile(context, SoundType.AFTER_MAX).absolutePath
            )
            verifyNoInteractions(resources)
            complete(players.constructed().last())
        }
    }

    @Test
    fun `disabling sounds prevents default and custom playback`() {
        `when`(preferences.getBoolean("play_sounds", true)).thenReturn(false)
        mockConstruction(MediaPlayer::class.java).use { players ->
            SoundHelper.playBeforeMaxSound(context)
            SoundHelper.playAfterMaxSound(context)
            org.junit.Assert.assertTrue(players.constructed().isEmpty())
        }
    }

    private fun complete(player: MediaPlayer) {
        val listener = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener::class.java)
        verify(player).setOnCompletionListener(listener.capture())
        listener.value.onCompletion(player)
        verify(player).release()
    }
}
