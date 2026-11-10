package com.mamenko.siphappens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

data class SoundOptionsState(val isBusy: Boolean = false, val error: Exception? = null)

class SoundOptionsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(SoundOptionsState())
    val state = mutableState.asStateFlow()

    fun importSound(context: Context, type: SoundType, uri: Uri) {
        val applicationContext = context.applicationContext
        changeSound { CustomSoundStore.importSound(applicationContext, type, uri) }
    }

    fun resetSound(context: Context, type: SoundType) {
        val applicationContext = context.applicationContext
        changeSound { CustomSoundStore.resetSound(applicationContext, type) }
    }

    fun clearError() {
        mutableState.value = mutableState.value.copy(error = null)
    }

    private fun changeSound(operation: () -> Unit) {
        viewModelScope.launch {
            mutableState.value = SoundOptionsState(isBusy = true)
            val error = try {
                withContext(Dispatchers.IO) { operation() }
                null
            } catch (error: IOException) {
                error
            } catch (error: IllegalArgumentException) {
                error
            } catch (error: IllegalStateException) {
                error
            } catch (error: SecurityException) {
                error
            }
            mutableState.value = SoundOptionsState(error = error)
        }
    }
}
