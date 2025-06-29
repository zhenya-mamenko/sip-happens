package com.siphappens

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "SipHappensPrefs"
    private const val KEY_COUNTER = "counter"
    private const val KEY_MAXIMUM = "maximum"
    private const val KEY_IMAGE_CODE = "image_code"
    private const val KEY_PLAY_SOUNDS = "play_sounds"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getCounter(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_COUNTER, 0)
    }

    fun setCounter(context: Context, counter: Int) {
        getSharedPreferences(context).edit()
            .putInt(KEY_COUNTER, counter)
            .apply()
    }

    fun getMaximum(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_MAXIMUM, 4)
    }

    fun setMaximum(context: Context, maximum: Int) {
        getSharedPreferences(context).edit()
            .putInt(KEY_MAXIMUM, maximum)
            .apply()
    }

    fun getImageCode(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_IMAGE_CODE, 1)
    }

    fun setImageCode(context: Context, imageCode: Int) {
        getSharedPreferences(context).edit()
            .putInt(KEY_IMAGE_CODE, imageCode)
            .apply()
    }

    fun getPlaySounds(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_PLAY_SOUNDS, true)
    }

    fun setPlaySounds(context: Context, playSounds: Boolean) {
        getSharedPreferences(context).edit()
            .putBoolean(KEY_PLAY_SOUNDS, playSounds)
            .apply()
    }
}
