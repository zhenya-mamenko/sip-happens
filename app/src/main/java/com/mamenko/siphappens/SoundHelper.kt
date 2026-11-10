package com.mamenko.siphappens

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import java.io.IOException

class SoundHelper {
    companion object {
        private var mediaPlayer: MediaPlayer? = null

        fun playBeforeMaxSound(context: Context) {
            playSound(context, SoundType.BEFORE_MAX)
        }

        fun playAfterMaxSound(context: Context) {
            playSound(context, SoundType.AFTER_MAX)
        }

        private fun playSound(context: Context, type: SoundType) {
            if (!PreferenceManager.getPlaySounds(context)) return

            stopCurrentSound()
            val player = MediaPlayer()
            mediaPlayer = player
            try {
                val customSound = CustomSoundStore.getFile(context, type)
                if (customSound.isFile) {
                    player.setDataSource(customSound.absolutePath)
                } else {
                    context.resources.openRawResourceFd(type.defaultResource).use { descriptor ->
                        player.setDataSource(
                            descriptor.fileDescriptor, descriptor.startOffset, descriptor.length
                        )
                    }
                }
                player.setOnCompletionListener { mp ->
                    mp.release()
                    if (mediaPlayer === mp) mediaPlayer = null
                }
                player.setOnErrorListener { mp, what, extra ->
                    Log.e("SoundHelper", "Playback failed: $what, $extra")
                    Toast.makeText(context, R.string.sound_playback_error, Toast.LENGTH_SHORT).show()
                    mp.release()
                    if (mediaPlayer === mp) mediaPlayer = null
                    true
                }
                player.prepare()
                player.start()
            } catch (error: IOException) {
                reportPlaybackError(context, error)
            } catch (error: IllegalArgumentException) {
                reportPlaybackError(context, error)
            } catch (error: IllegalStateException) {
                reportPlaybackError(context, error)
            } catch (error: SecurityException) {
                reportPlaybackError(context, error)
            }
        }

        private fun reportPlaybackError(context: Context, error: Exception) {
            Log.e("SoundHelper", "Cannot play sound", error)
            stopCurrentSound()
            Toast.makeText(context, R.string.sound_playback_error, Toast.LENGTH_SHORT).show()
        }

        private fun stopCurrentSound() {
            mediaPlayer?.let { mp ->
                mp.release()
                mediaPlayer = null
            }
        }
    }
}
