package com.siphappens

import android.content.Context
import android.media.MediaPlayer

class SoundHelper {
    companion object {
        private var mediaPlayer: MediaPlayer? = null

        fun playBeforeMaxSound(context: Context) {
            if (!PreferenceManager.getPlaySounds(context)) return

            try {
                stopCurrentSound()
                mediaPlayer = MediaPlayer.create(context, R.raw.sound_before_max)
                mediaPlayer?.setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
                mediaPlayer?.start()
            } catch (_: Exception) {
            }
        }

        fun playAfterMaxSound(context: Context) {
            if (!PreferenceManager.getPlaySounds(context)) return

            try {
                stopCurrentSound()
                mediaPlayer = MediaPlayer.create(context, R.raw.sound_after_max)
                mediaPlayer?.setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                }
                mediaPlayer?.start()
            } catch (_: Exception) {
            }
        }

        private fun stopCurrentSound() {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
                mediaPlayer = null
            }
        }
    }
}
