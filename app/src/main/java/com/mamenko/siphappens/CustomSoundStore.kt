package com.mamenko.siphappens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

enum class SoundType(val fileName: String, @RawRes val defaultResource: Int) {
    BEFORE_MAX("before_max", R.raw.sound_before_max),
    AFTER_MAX("after_max", R.raw.sound_after_max)
}

object CustomSoundStore {
    const val MAX_DURATION_MS = 3_000

    fun getFile(context: Context, type: SoundType): File =
        File(context.filesDir, "custom_sounds/${type.fileName}")

    fun hasCustomSound(context: Context, type: SoundType): Boolean =
        getFile(context, type).isFile

    fun importSound(context: Context, type: SoundType, uri: Uri) {
        val destination = getFile(context, type)
        val directory = destination.parentFile ?: throw IOException("Missing sound directory")
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Cannot create sound directory")
        }
        val temporary = File.createTempFile("sound_", ".tmp", directory)
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open selected sound")
            input.use { source ->
                temporary.outputStream().use { target -> source.copyTo(target) }
            }
            val player = MediaPlayer()
            try {
                player.setDataSource(temporary.absolutePath)
                player.prepare()
                requireValidDuration(player.duration)
            } finally {
                player.release()
            }
            // Rename only after validation so a rejected file cannot replace a working sound.
            Files.move(
                temporary.toPath(), destination.toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING
            )
        } finally {
            if (temporary.exists() && !temporary.delete()) {
                Log.w("CustomSoundStore", "Cannot remove temporary sound")
            }
        }
    }

    internal fun requireValidDuration(durationMs: Int) {
        require(durationMs in 1..MAX_DURATION_MS) {
            "Sound duration must be between 1 and $MAX_DURATION_MS ms"
        }
    }

    fun resetSound(context: Context, type: SoundType) {
        val file = getFile(context, type)
        if (file.exists() && !file.delete()) {
            throw IOException("Cannot remove custom sound")
        }
    }
}
